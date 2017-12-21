package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.List;

public class AliasList extends ListView<Alias> {

    private List<Alias> aliasList;

    public AliasList() {
        setCellFactory(param -> new AliasListCell());
    }

    private void openEditDialog() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        AliasDialog dialog = new AliasDialog();
        dialog.setAliasContent(selected);
        dialog.showAndWait();
        if (dialog.getResult() == ButtonType.OK) {
            dialog.applyTo(selected);
            refresh();
        }
    }


    private void openNewDialog() {
        AliasDialog dialog = new AliasDialog();
        dialog.showAndWait();
        if (dialog.getResult() == ButtonType.OK) {
            Alias a = dialog.createAlias();
            getItems().add(a);
        }
    }

    /**
     * Deletes the selection {@link Alias} from the list.
     */
    private void deleteSelectedAlias() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        getItems().remove(selected);
    }

    /**
     * Sets the Aliases content for this list.
     *
     * @param list
     */
    public void setAliases(final ObservableList<Alias> list) {
        setItems(list);
    }

    /**
     * Custom renderer for every {@link Alias} in the ListView.
     */
    private class AliasListCell extends ListCell<Alias> {

        private ContextMenu contextMenu = new ContextMenu();
        private ContextMenu contextMenu2 = new ContextMenu();

        public AliasListCell() {
            MenuItem newItem = new MenuItem("New alias...");
            MenuItem newItem2 = new MenuItem("New alias...");
            MenuItem editItem = new MenuItem("Edit");
            MenuItem duplicateItem = new MenuItem("Duplicate");
            MenuItem deleteItem = new MenuItem("Delete");

            newItem2.setOnAction(e -> openNewDialog());
            editItem.setOnAction(e -> openEditDialog());
            deleteItem.setOnAction(e -> deleteSelectedAlias());

            contextMenu.getItems().add(newItem);
            contextMenu.getItems().add(editItem);
            contextMenu.getItems().add(duplicateItem);
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(deleteItem);

            contextMenu2.getItems().add(newItem2);
        }


        @Override
        protected void updateItem(Alias item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setTooltip(null);
                // When we right-clicked on an empty cell, show a different context menu.
                if (!isEditing()) {
                    setContextMenu(contextMenu2);
                }
                return;
            }

            if (!isEditing()) {
                setContextMenu(contextMenu);
            }

            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));
        }
    }
}
