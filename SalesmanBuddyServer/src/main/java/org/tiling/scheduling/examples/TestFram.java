package org.tiling.scheduling.examples;

/*
 * SimpleTableDemo.java requires no other files.
 */

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TestFram extends JPanel implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2840542364728295717L;
	private boolean DEBUG = false;
    
   

/** Listens to the combo box. */
@SuppressWarnings("rawtypes")
public void actionPerformed(ActionEvent e) {
    JComboBox cb = (JComboBox)e.getSource();
    String petName = (String)cb.getSelectedItem();
}
    public TestFram() {
        super(new GridLayout(1,0));
        String[] columnNames = {"Tasks",
                                "Time",
                                "Sport",
                                "Status",
                                "Creator"};
        String[] petStrings = { "Bird", "Cat", "Dog", "Rabbit", "Pig" };
        String[] status = { "Active", "Inactive"};

        //Create the combo box, select the item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        JComboBox petList = new JComboBox(petStrings);
        petList.setSelectedIndex(4);
        petList.addActionListener(this);

        JComboBox statusList = new JComboBox(status);
        statusList.setSelectedIndex(1);
        statusList.addActionListener(this);
        
        /*Object[][] data = {
            {"Mary", "Campione",
             "Snowboarding", new Integer(5), new Boolean(false)},
            {"Alison", "Huml",
             "Rowing", new Integer(3), new Boolean(true)},
            {"Kathy", "Walrath",
             "Knitting", new Integer(2), new Boolean(false)},
            {"Sharon", "Zakhour",
             "Speed reading", new Integer(20), new Boolean(true)},
            {"Philip", "Milne",
             "Pool", new Integer(10), new Boolean(false)}
        };*/

//        final JTable table = new JTable(data, columnNames);
        JTable table = new JTable();
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        model.addColumn("Task Type", new Object[]{""});
        model.addColumn("Change", new Object[]{"sample"});
        model.addColumn("Status", new Object[]{""});
        model.addColumn("Creator", new Object[]{"Mickey"});
        
        JButton button=new JButton("Add Task");
        button.setActionCommand("enable");
        button.setEnabled(false);
        button.addActionListener(this);
//        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(petList));
        
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setCellEditor(new DefaultCellEditor(petList));
        TableColumn col1 = table.getColumnModel().getColumn(2);
        col1.setCellEditor(new DefaultCellEditor(statusList));
//        col.setCellRenderer(new MyComboBoxRenderer(petStrings));
        
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);

        /*if (DEBUG) {
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    printDebugData(table);
                }
            });
        }*/

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
//        add(petList);
        //Add the scrollpane to this panel.
        
        add(scrollPane);
        add(button);
    }

    private void printDebugData(JTable table) {
        int numRows = table.getRowCount();
        int numCols = table.getColumnCount();
        javax.swing.table.TableModel model = table.getModel();

        System.out.println("Value of data: ");
        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + model.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Java Scheduler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(new Dimension(271, 85));
        //Create and set up the content pane.
        TestFram newContentPane = new TestFram();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
