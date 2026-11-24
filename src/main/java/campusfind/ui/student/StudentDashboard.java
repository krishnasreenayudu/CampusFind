package campusfind.ui.student;

import campusfind.ui.common.BaseDashboard;

public class StudentDashboard extends BaseDashboard {
    public StudentDashboard() {
        super("Student");
        addPage("Home", new StudentHomePanel());
        addPage("Report Item", new ReportItemPanel());
        addPage("Search Items", new SearchItemsPanel());
        addPage("My Reports", new MyReportsPanel());
        addPage("My Claims", new MyClaimsPanel());
        show("Home");
    }
}
