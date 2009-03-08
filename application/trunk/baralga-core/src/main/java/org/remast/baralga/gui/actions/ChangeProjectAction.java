package org.remast.baralga.gui.actions;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.remast.baralga.gui.model.PresentationModel;
import org.remast.baralga.gui.model.ProjectActivityStateException;
import org.remast.baralga.model.Project;
import org.remast.util.TextResourceBundle;

/**
 * Action to change the active project.
 * @author remast
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class ChangeProjectAction extends AbstractBaralgaAction {

    private static final Log log = LogFactory.getLog(ExportDataAction.class);

    /** The bundle for internationalized texts. */
    private static final TextResourceBundle textBundle = TextResourceBundle.getBundle(ChangeProjectAction.class);

    /**
     * The project to be activated when the action is performed.
     */
    private Project newProject;

    public ChangeProjectAction(final PresentationModel model, final Project newProject) {
        super(model);
        this.newProject = newProject;

        // Highlight the currently selected project
        String projectName = String.valueOf(newProject);
        if (model.getSelectedProject() != null && model.getSelectedProject().equals(newProject)) {
            projectName = "* " + projectName;
        }

        putValue(NAME, projectName);
        putValue(SHORT_DESCRIPTION, textBundle.textFor("ChangeProjectAction.ShortDescription") + String.valueOf(newProject) + "."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void actionPerformed(final ActionEvent event) {
        // Check if the new project is different from the old one
        if (ObjectUtils.equals(getModel().getSelectedProject(), newProject)) {
            return;
        }
        
        getModel().changeProject(newProject);

        if (!getModel().isActive() && isStartConfirmed()) {
            try {
                getModel().start();
            } catch (ProjectActivityStateException e) {
                // Ignore as we have already checked before that the project is active.
            }
        }
    }

    /**
     * Checks with user wether newly selected project should be started or not.
     * @return <code>true</code> if project shall be started else <code>false</code>
     */
    private boolean isStartConfirmed() {
        // Unfortunately the systray gives no hint where it is located, so we have to guess
        // by getting the current mouse location.
        final Point currentMousePosition = MouseInfo.getPointerInfo().getLocation();

        final JOptionPane pane = new JOptionPane(
                textBundle.textFor("StartActivityConfirmDialog.Message"), //$NON-NLS-1$
                JOptionPane.QUESTION_MESSAGE, 
                JOptionPane.YES_NO_OPTION
                );

        final JDialog dialog = pane.createDialog(textBundle.textFor("StartActivityConfirmDialog.Title")); //$NON-NLS-1$
        dialog.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/Baralga-Tray.gif"))); //$NON-NLS-1$ 
        
        final Dimension d = dialog.getPreferredSize();
        // TODO: check that dialog stays within screen boundaries
        dialog.setLocation(currentMousePosition.x - d.width, currentMousePosition.y - d.height);

        dialog.setVisible(true);
        dialog.dispose();

        final Object selectedValue = pane.getValue();

        return (selectedValue instanceof Integer)
        && (((Integer) selectedValue).intValue() == JOptionPane.YES_OPTION);
    }
}
