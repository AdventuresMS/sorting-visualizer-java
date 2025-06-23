import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SortingVisualizer extends JFrame {
    public SortingVisualizer() {
        setTitle("Monika's Sorting Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        add(new SortingPanel());
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SortingVisualizer::new);
    }
}

class SortingPanel extends JPanel implements ActionListener {
    private int[] array;
    private boolean[] sorted;
    private Timer timer;
    private int i = 0, j = 0, step = 0, comparisons = 0;
    private JComboBox<String> algorithmSelector;
    private JButton shuffleButton, startButton, pauseButton, stepButton;
    private JSlider speedSlider;
    private String currentAlgorithm = "Bubble Sort";
    private boolean isSorting = false, isPaused = false;
    private int[] temp;
    private int pivotIndex = -1;

    public SortingPanel() {
        setBackground(Color.BLACK);
        array = generateArray(100);
        sorted = new boolean[array.length];

        algorithmSelector = new JComboBox<>(new String[] {"Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort"});
        shuffleButton = new JButton("Shuffle");
        startButton = new JButton("Start Sorting");
        pauseButton = new JButton("Pause");
        stepButton = new JButton("Step");
        speedSlider = new JSlider(1, 100, 20);

        timer = new Timer(speedSlider.getValue(), this);
        speedSlider.addChangeListener(_ -> timer.setDelay(speedSlider.getValue()));

        shuffleButton.addActionListener(_ -> {
            array = generateArray(100);
            sorted = new boolean[array.length];
            i = j = step = comparisons = 0;
            pivotIndex = -1;
            isSorting = false;
            repaint();
        });

        startButton.addActionListener(_ -> {
            currentAlgorithm = (String) algorithmSelector.getSelectedItem();
            i = j = step = comparisons = 0;
            sorted = new boolean[array.length];
            pivotIndex = -1;
            isSorting = true;
            isPaused = false;
            if (currentAlgorithm.equals("Merge Sort")) temp = new int[array.length];
            if (currentAlgorithm.equals("Quick Sort")) step = array.length - 1;
            timer.start();
        });

        pauseButton.addActionListener(_ -> {
            if (isPaused) {
                timer.start();
                pauseButton.setText("Pause");
            } else {
                timer.stop();
                pauseButton.setText("Resume");
            }
            isPaused = !isPaused;
        });

        stepButton.addActionListener(_ -> {
            if (!isSorting) return;
            timer.stop();
            actionPerformed(null);
        });

        JPanel controls = new JPanel();
        controls.add(new JLabel("Algorithm:"));
        controls.add(algorithmSelector);
        controls.add(shuffleButton);
        controls.add(startButton);
        controls.add(pauseButton);
        controls.add(stepButton);
        controls.add(new JLabel("Speed:"));
        controls.add(speedSlider);

        setLayout(new BorderLayout());
        add(controls, BorderLayout.SOUTH);
    }

    private int[] generateArray(int size) {
        int[] arr = new int[size];
        Random rand = new Random();
        int height = getHeight() > 0 ? getHeight() : 500;
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(height - 100) + 50;
        }
        return arr;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth() / array.length;
        for (int k = 0; k < array.length; k++) {
            if (sorted[k]) {
                g.setColor(Color.GREEN);
            } else if (k == pivotIndex) {
                g.setColor(Color.YELLOW);
            } else {
                float hue = (float) k / array.length;
                g.setColor(Color.getHSBColor(0.6f, 1.0f, 1.0f - hue));
            }
            g.fillRect(k * width, getHeight() - array[k], width, array[k]);
        }
        g.setColor(Color.WHITE);
        g.drawString("Comparisons: " + comparisons, 10, 20);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (currentAlgorithm) {
            case "Bubble Sort": bubbleSortStep(); break;
            case "Selection Sort": selectionSortStep(); break;
            case "Insertion Sort": insertionSortStep(); break;
            case "Merge Sort": mergeSortStep(); break;
            case "Quick Sort": quickSortStep(); break;
        }
    }

    private void bubbleSortStep() {
        if (i < array.length - 1) {
            if (j < array.length - i - 1) {
                comparisons++;
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
                j++;
            } else {
                sorted[array.length - i - 1] = true;
                j = 0;
                i++;
            }
            repaint();
        } else {
            sorted[0] = true;
            timer.stop();
        }
    }

    private void selectionSortStep() {
        if (i < array.length - 1) {
            int minIndex = i;
            for (int k = i + 1; k < array.length; k++) {
                comparisons++;
                if (array[k] < array[minIndex]) minIndex = k;
            }
            int temp = array[i];
            array[i] = array[minIndex];
            array[minIndex] = temp;
            sorted[i] = true;
            i++;
            repaint();
        } else {
            sorted[array.length - 1] = true;
            timer.stop();
        }
    }

    private void insertionSortStep() {
        if (i < array.length) {
            int key = array[i];
            int k = i - 1;
            while (k >= 0 && array[k] > key) {
                comparisons++;
                array[k + 1] = array[k];
                k--;
            }
            if (k >= 0) comparisons++;
            array[k + 1] = key;
            sorted[i] = true;
            i++;
            repaint();
        } else timer.stop();
    }

    private void mergeSortStep() {
        if (step == 0) step = 1;
        boolean done = true;
        for (int start = 0; start < array.length - 1; start += 2 * step) {
            int mid = Math.min(start + step - 1, array.length - 1);
            int end = Math.min(start + 2 * step - 1, array.length - 1);
            merge(start, mid, end);
            done = false;
        }
        step *= 2;
        repaint();
        if (done || step > array.length) {
            for (int k = 0; k < array.length; k++) sorted[k] = true;
            timer.stop();
        }
    }

    private void merge(int l, int m, int r) {
        for (int k = l; k <= r; k++) temp[k] = array[k];
        int i = l, j = m + 1, k = l;
        while (i <= m && j <= r) {
            comparisons++;
            if (temp[i] <= temp[j]) array[k++] = temp[i++];
            else array[k++] = temp[j++];
        }
        while (i <= m) array[k++] = temp[i++];
        while (j <= r) array[k++] = temp[j++];
    }

    private void quickSortStep() {
        if (step <= 0) {
            for (int k = 0; k < array.length; k++) sorted[k] = true;
            timer.stop();
            return;
        }
        pivotIndex = step;
        quickSort(0, step);
        repaint();
        step -= 1;
    }

    private void quickSort(int low, int high) {
        if (low < high) {
            int pi = partition(low, high);
            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    private int partition(int low, int high) {
        pivotIndex = high;
        int pivot = array[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            comparisons++;
            if (array[j] < pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        sorted[i + 1] = true;
        return i + 1;
    }
}
