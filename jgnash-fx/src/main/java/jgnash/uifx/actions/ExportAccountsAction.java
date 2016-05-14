/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2016 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.uifx.actions;

import java.io.File;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import jgnash.engine.AccountTreeXMLFactory;
import jgnash.engine.EngineFactory;
import jgnash.uifx.views.main.MainApplication;
import jgnash.util.FileUtils;
import jgnash.util.ResourceUtils;

/**
 * UI Action to export the current account tree
 *
 * @author Craig Cavanaugh
 */
public class ExportAccountsAction {

    private static final String LAST_DIR = "exportDir";

    private ExportAccountsAction() {
        // Utility class
    }

    public static void showAndWait() {
        final ResourceBundle resources = ResourceUtils.getBundle();

        final FileChooser fileChooser = configureFileChooser();
        fileChooser.setTitle(resources.getString("Title.SelFile"));

        final File file = fileChooser.showSaveDialog(MainApplication.getInstance().getPrimaryStage());

        if (file != null) {
            Preferences pref = Preferences.userNodeForPackage(ExportAccountsAction.class);
            pref.put(LAST_DIR, file.getParentFile().getAbsolutePath());

            final ExportTask exportTask =
                    new ExportTask(new File(FileUtils.stripFileExtension(file.getAbsolutePath()) + ".xml"));

            new Thread(exportTask).start();

            MainApplication.getInstance().setBusy(exportTask);
        }
    }

    private static FileChooser configureFileChooser() {
        final Preferences pref = Preferences.userNodeForPackage(ExportAccountsAction.class);
        final FileChooser fileChooser = new FileChooser();

        fileChooser.setInitialDirectory(new File(pref.get(LAST_DIR, System.getProperty("user.home"))));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        ResourceUtils.getString("Label.XMLFiles") + " (*.xml)", "*.xml", "*.XML")
        );

        return fileChooser;
    }

    private static class ExportTask extends Task<Void> {
        private final File file;

        ExportTask(final File file) {
            this.file = file;
        }

        @Override
        protected Void call() throws Exception {
            updateMessage(ResourceUtils.getString("Message.PleaseWait"));
            updateProgress(-1, Long.MAX_VALUE);

            AccountTreeXMLFactory.exportAccountTree(EngineFactory.getEngine(EngineFactory.DEFAULT), file);

            return null;
        }
    }
}