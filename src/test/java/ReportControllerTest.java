import com.cab302.wellbeing.controller.ReportController;
import com.cab302.wellbeing.controller.BarChartModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportControllerTest {

    private ReportController reportController;

    @BeforeEach
    public void setUp() {
        reportController = new ReportController();
    }

    @Test
    public void testSortDataset_BubbleSort() {
        List<BarChartModel> dataset = new ArrayList<>();
        dataset.add(new BarChartModel("url1", 20));
        dataset.add(new BarChartModel("url2", 50));
        dataset.add(new BarChartModel("url3", 10));

        List<BarChartModel> sortedDataset = reportController.sortDataset(dataset);

        assertEquals(50, sortedDataset.get(0).durationSum);
        assertEquals(20, sortedDataset.get(1).durationSum);
        assertEquals(10, sortedDataset.get(2).durationSum);
    }

    @Test
    public void testGetTop5_LessThan5Entries() {
        List<BarChartModel> dataset = new ArrayList<>();
        dataset.add(new BarChartModel("url1", 20));
        dataset.add(new BarChartModel("url2", 50));

        List<BarChartModel> top5 = reportController.getTop5(dataset);

        assertEquals(2, top5.size());
        assertEquals(50, top5.get(0).durationSum);
        assertEquals(20, top5.get(1).durationSum);
    }

    @Test
    public void testGetTop5_MoreThan5Entries() {
        List<BarChartModel> dataset = new ArrayList<>();
        dataset.add(new BarChartModel("url1", 20));
        dataset.add(new BarChartModel("url2", 50));
        dataset.add(new BarChartModel("url3", 10));
        dataset.add(new BarChartModel("url4", 70));
        dataset.add(new BarChartModel("url5", 30));
        dataset.add(new BarChartModel("url6", 60));

        List<BarChartModel> top5 = reportController.getTop5(dataset);

        assertEquals(5, top5.size());
        assertEquals(70, top5.get(0).durationSum);
        assertEquals(60, top5.get(1).durationSum);
        assertEquals(50, top5.get(2).durationSum);
        assertEquals(30, top5.get(3).durationSum);
        assertEquals(20, top5.get(4).durationSum);
    }
}