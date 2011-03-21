/**
 * Copyright (c) 2009-2010 MATSUFUJI Hideharu <matsufuji2008@gmail.com>,
 *               2010-2011 KUBO Atsuhiro <kubo@iteman.jp>,
 * All rights reserved.
 *
 * This file is part of MakeGood.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.piece_framework.makegood.launch;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchConfigurationWorkingCopy;
import org.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.php.internal.debug.core.PHPDebugPlugin;
import org.eclipse.php.internal.debug.core.launching.PHPLaunchDelegateProxy;
import org.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org.eclipse.php.internal.debug.core.preferences.PHPDebuggersRegistry;
import org.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org.eclipse.php.internal.debug.core.preferences.PHPexeItem;

import com.piece_framework.makegood.stagehand_testrunner.StagehandTestRunner;

public class MakeGoodLaunchConfigurationDelegate extends PHPLaunchDelegateProxy {
    private static final String MAKEGOOD_JUNIT_XML_FILE = "MAKEGOOD_JUNIT_XML_FILE"; //$NON-NLS-1$
    private String delegateClass;

    @Override
    public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        if (!configuration.exists()) {
            cancelLaunch(monitor);
            return false;
        }

        boolean result;
        try {
            result = super.finalLaunchCheck(configuration, mode, monitor);
        } catch (DebugException e) {
            cancelLaunch(monitor);
            return false;
        } catch (CoreException e) {
            cancelLaunch(monitor);
            throw e;
        }
        if (!result) {
            cancelLaunch(monitor);
        }
        return result;
    }

    @Override
    public ILaunch getLaunch(ILaunchConfiguration originalConfiguration, String mode) throws CoreException {
        ILaunchConfiguration configuration = null;
        try {
            configuration = createConfiguration(originalConfiguration);
        } catch (CoreException e) {
            cancelLaunch();
            throw e;
        }

        delegateClass = configuration.getAttribute(PHPDebugCorePreferenceNames.CONFIGURATION_DELEGATE_CLASS, ""); //$NON-NLS-1$
        return new MakeGoodLaunch(configuration, mode, null);
    }

    @Override
    public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        if (!configuration.exists()) {
            cancelLaunch(monitor);
            return false;
        }

        boolean result;
        try {
            result = super.preLaunchCheck(configuration, mode, monitor);
        } catch (DebugException e) {
            cancelLaunch(monitor);
            return false;
        } catch (CoreException e) {
            cancelLaunch(monitor);
            throw e;
        }
        if (!result) {
            cancelLaunch(monitor);
        }
        return result;
    }

    @Override
    public void launch(
        ILaunchConfiguration originalConfiguration,
        String mode,
        ILaunch launch,
        IProgressMonitor monitor) throws CoreException {
        ILaunchConfigurationWorkingCopy workingCopy = (ILaunchConfigurationWorkingCopy) launch.getLaunchConfiguration();
        if (workingCopy == null) {
            cancelLaunch(monitor);
            return;
        }

        if (!workingCopy.exists()) {
            cancelLaunch(monitor);
            return;
        }

        try {
            JUnitXMLRegistry.create();
        } catch (SecurityException e) {
            cancelLaunch(monitor);
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }

        try {
            if (ILaunchManager.RUN_MODE.equals(mode) && TestingTargets.getInstance().isAllTests()) {
                ((ILaunchConfigurationWorkingCopy) workingCopy).setAttribute(IPHPDebugConstants.RUN_WITH_DEBUG_INFO, false);
                super.launch(workingCopy, mode, launch, monitor);
            } else {
                super.launch(workingCopy, mode, launch, monitor);
            }
        } catch (CoreException e) {
            cancelLaunch(monitor);
            throw e;
        }
    }

    @Override
    /**
     * @since 1.2.0
     */
    protected ILaunchConfigurationDelegate2 getConfigurationDelegate(ILaunchConfiguration configuration) throws CoreException {
        if (launchConfigurationDelegate == null) {
            try {
                if (delegateClass.length() == 0) {
                    throw new IllegalArgumentException();
                }

                launchConfigurationDelegate = (ILaunchConfigurationDelegate2) Class
                .forName(delegateClass).newInstance();
            } catch (Throwable t) {
                throw new CoreException(new Status(IStatus.ERROR,
                        PHPDebugPlugin.ID, 0,
                        "Launch configuration delegate loading error.", t));
            }
        }
        return launchConfigurationDelegate;
    }

    private ILaunchConfiguration createConfiguration(
        ILaunchConfiguration configuration) throws CoreException {
        String configurationName =
            "MakeGood" + Long.toString(System.currentTimeMillis()); //$NON-NLS-1$
        String junitXMLFile =
            JUnitXMLRegistry.getRegistry().getAbsolutePath().toString() +
            String.valueOf(File.separatorChar) +
            configurationName +
            ".xml"; //$NON-NLS-1$

        ILaunchConfigurationWorkingCopy workingCopy = new LaunchConfigurationWorkingCopy((LaunchConfiguration) configuration) {
            @Override
            public synchronized ILaunchConfiguration doSave() throws CoreException {
                return null;
            }
        };

        String mainScript = TestingTargets.getInstance().getMainScript();
        if (mainScript == null) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The main script is not found.")); //$NON-NLS-1$
        }

        workingCopy.setAttribute(IPHPDebugConstants.ATTR_FILE, mainScript);

        workingCopy.setAttribute(
            IPHPDebugConstants.ATTR_FILE_FULL_PATH, getCommandPath()
        );
        workingCopy.setAttribute(MAKEGOOD_JUNIT_XML_FILE, junitXMLFile);
        workingCopy.setAttribute(
            IDebugParametersKeys.EXE_CONFIG_PROGRAM_ARGUMENTS,
            TestingTargets.getInstance().generateCommandLine(junitXMLFile)
        );

        IResource mainScriptResource = TestingTargets.getInstance().getMainScriptResource();
        if (mainScriptResource == null) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The main script resource is not found.")); //$NON-NLS-1$
        }

        IProject project = mainScriptResource.getProject();
        if (project != null && project.exists()) {
            workingCopy.setAttribute(IPHPDebugConstants.PHP_Project, project.getName());
            rewriteBasicConfigurationAttributes(workingCopy, project);
            TestingTargets.getInstance().setProject(project);
        }

        return workingCopy;
    }

    public static String getCommandPath() throws CoreException {
        return StagehandTestRunner.getCommandPath(
                   TestingTargets.getInstance().getTestingFramework().name()
               );
    }

    public static String getJUnitXMLFile(ILaunch launch) throws CoreException {
        return launch.getLaunchConfiguration().getAttribute(MAKEGOOD_JUNIT_XML_FILE, (String) null);
    }

    private void rewriteBasicConfigurationAttributes(ILaunchConfigurationWorkingCopy workingCopy, IProject project) {
        PHPexeItem phpexeItem = PHPexeItemFactory.create(project);
        if (phpexeItem == null) return;

        workingCopy.setAttribute(PHPDebugCorePreferenceNames.PHP_DEBUGGER_ID, phpexeItem.getDebuggerID());
        workingCopy.setAttribute(PHPDebugCorePreferenceNames.CONFIGURATION_DELEGATE_CLASS, PHPDebuggersRegistry.getDebuggerConfiguration(phpexeItem.getDebuggerID()).getScriptLaunchDelegateClass());
        workingCopy.setAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, phpexeItem.getExecutable().getAbsolutePath().toString());
        workingCopy.setAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, phpexeItem.getINILocation() != null ? phpexeItem.getINILocation().toString() : null);
        workingCopy.setAttribute(IPHPDebugConstants.RUN_WITH_DEBUG_INFO, PHPDebugPlugin.getDebugInfoOption());
        workingCopy.setAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, PHPProjectPreferences.getStopAtFirstLine(project));
    }

    private void cancelLaunch() {
        TestLifecycle.destroy();
    }

    private void cancelLaunch(IProgressMonitor monitor) {
        monitor.setCanceled(true);
        cancelLaunch();
    }
}
