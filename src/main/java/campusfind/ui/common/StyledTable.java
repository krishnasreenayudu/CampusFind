package campusfind.ui.common;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.function.IntPredicate;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/** Read-only, sortable table with zebra rows and optional red highlighting of chosen rows. */
public class StyledTable extends JTable {
    private IntPredicate highlight;

    public StyledTable(String... columns) {
        super(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        });
        setRowHeight(30);
        setFont(Theme.BODY);
        getTableHeader().setFont(Theme.BODY_BOLD);
        getTableHeader().setReorderingAllowed(false);
        setShowVerticalLines(false);
        setGridColor(Theme.BORDER);
        setSelectionBackground(new Color(191, 219, 254));
        setSelectionForeground(Theme.TEXT);
        setAutoCreateRowSorter(true);
        setFillsViewportHeight(true);
    }

    public void setRows(List<Object[]> rows) {
        DefaultTableModel m = (DefaultTableModel) getModel();
        m.setRowCount(0);
        rows.forEach(m::addRow);
    }

    /** Index into the list that was used to fill the table (works even after the user sorts), or -1. */
    public int selectedIndex() {
        int r = getSelectedRow();
        return r < 0 ? -1 : convertRowIndexToModel(r);
    }

    /** Rows for which the test returns true are painted light red. */
    public void setHighlight(IntPredicate test) { this.highlight = test; }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component c = super.prepareRenderer(renderer, row, col);
        if (!isRowSelected(row)) {
            boolean red = highlight != null && highlight.test(convertRowIndexToModel(row));
            c.setBackground(red ? new Color(254, 226, 226) : (row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251)));
            c.setForeground(red ? new Color(153, 27, 27) : Theme.TEXT);
        }
        return c;
    }

    public JScrollPane scroll() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }
}
