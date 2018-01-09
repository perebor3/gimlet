package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.QueryExecuteEvent;
import cruft.wtf.gimlet.event.QuerySavedEvent;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;

import java.util.List;

public class QueryTree extends TreeView<Query> {

    /**
     * The original assigned query list.
     */
    private ObservableList<Query> queryList;

    public QueryTree() {
        EventDispatcher.getInstance().register(this);
    }

    /**
     * Sets the list of queries to display in the tree.
     *
     * @param queryList The list of queries.
     */
    public void setQueryList(final ObservableList<Query> queryList) {
        this.queryList = queryList;

        TreeItem<Query> root = new TreeItem<>();

        addQuery(root, queryList);

        setShowRoot(false);
        setCellFactory(param -> new QueryConfigurationTreeCell());

        setRoot(root);
    }

    /**
     * Adds queries recursively.
     *
     * @param root
     * @param queryList
     */
    private void addQuery(final TreeItem<Query> root, List<Query> queryList) {
        if (queryList == null || queryList.size() == 0) {
            return;
        }

        for (Query q : queryList) {
            TreeItem<Query> qitem = new TreeItem<>(q);
            root.getChildren().add(qitem);
            addQuery(qitem, q.getSubQueries());
        }

        root.setExpanded(true);
    }

    private void executeSelectedQuery(final Query query) {
        QueryExecuteEvent e = new QueryExecuteEvent();
        e.setQuery(query);
        EventDispatcher.getInstance().post(e);
    }

    /**
     * Opens the {@link QueryEditDialog} based on the current selected {@link Query} in the tree.
     */
    private void openQueryEditDialog() {
        TreeItem<Query> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        QueryEditDialog qed = new QueryEditDialog(selectedItem.getValue());
        qed.showAndWait();
    }

    /**
     * Removes the selected query (and its children, ancestors and the whole shebang). This is done by updating the
     * model (the query list) and then forcefully refreshing the whole tree by re-adding the query list again.
     * <p>
     * We'll see how this works out. It can also be done somewhat 'prettier' for the user by deleting the TreeItem, and
     * then also remove it from the backing query list. The improvement on this is that the tree isn't reinitialized
     * (and thus the expand levels are kept the same).
     */
    private void removeSelectedQuery() {
        TreeItem<Query> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        alertConfirm.setTitle("Confirm deletion");
        alertConfirm.setHeaderText("Delete '" + selectedItem.getValue().getName() + "'?\nThis will delete all children nodes!");
        ((Button) alertConfirm.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
        ((Button) alertConfirm.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
        alertConfirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Query parent = selectedItem.getParent().getValue();
                if (parent == null) {
                    // the actual parent of parents. This TreeItem does not have a Query value.
                    queryList.remove(selectedItem.getValue());
                } else {
                    parent.getSubQueries().remove(selectedItem.getValue());
                }
                refresh();
            }
        });
    }


    /**
     * When a {@link Query} is saved through a {@link QueryEditDialog}, the {@link EventDispatcher} will notify this
     * control that it happened. Since there's no bidirectional binding (we only want to save things to the Query once
     * the user has pressed "OK"), we have to manually refresh the tree.
     *
     * @param event The event that the query has been saved.
     */
    @Subscribe
    public void onQuerySaved(QuerySavedEvent event) {
        // Refresh the tree ourselves. There's no bidirectional binding.
        refresh();
    }

    /**
     * Contains rendering logic for {@link Query} objects used throughout Gimlet.
     */
    private class QueryConfigurationTreeCell extends TextFieldTreeCell<Query> {

        private MenuItem menuItemExecute;

        private ContextMenu menu = new ContextMenu();

        public QueryConfigurationTreeCell() {
            menuItemExecute = new MenuItem("Run query", Images.COG.imageView());

            MenuItem editItem = new MenuItem("Edit");
            MenuItem removeItem = new MenuItem("Delete");

            menu.getItems().addAll(
                    menuItemExecute,
                    new SeparatorMenuItem(),
                    editItem,
                    new SeparatorMenuItem(),
                    removeItem);

            // Funky binding. We bind the 'disabled' property of the menu item, to the boolean property
            // of the editorTabViews's boolean value whether a tab is opened or not.
            menuItemExecute.disableProperty().bind(GimletApp.editorTabView.tabSelectedProperty().not());

            menuItemExecute.setOnAction(e -> executeSelectedQuery(getItem()));
            editItem.setOnAction(e -> openQueryEditDialog());
            removeItem.setOnAction(e -> removeSelectedQuery());
        }

        @Override
        public void updateItem(Query item, boolean empty) {
            // super call is required, see documentation.
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }

            if (!isEditing()) {
                this.setContextMenu(menu);
            }

            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));
        }
    }
}