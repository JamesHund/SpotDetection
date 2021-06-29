import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This class provides a GUI for spot detection. It serves primarily as a client
 * for ImageLib. The GUI has tweakable parameters for each stage of spot
 * detection and has the option to view each stage in the spot detection
 * process.
 */
public class GUI extends JFrame {

  private final int offset = 300;
  private final Integer[] radii = {4, 5, 6, 7, 8, 9, 10, 11};
  private final String genMessage = "GENERATING...";
  private final String doneMessage = "DONE GENERATING";
  private final String[] modes = {"Original", "Greyscale", "Noise "
      + "Reduction",
      "Edge "
          + "Detection", "Spot Detection"};

  //important GUI components
  private JLabel modeLabel, epsilonLabel, lowerLabel, upperLabel,
      generatingLabel, spotCountLabel;
  private JLabel imageLabel;
  private JComboBox<String> modeSelect;
  private JSlider epsilonSlider;
  private JTextField epsilonTextField;
  private JComboBox<Integer> lowerBoundSelect;
  private JComboBox<Integer> upperBoundSelect;
  private JButton generateButton;

  //fields for various stages of spot-detection
  private BufferedImage origImage;
  private BufferedImage greyScale;
  private BufferedImage nr;
  private BufferedImage ed;
  private SpotDetector sd;

  //spot-detection parameters
  private int mode = 0;
  private int epsilon = 50;
  private int lowerBound = 4;
  private int upperBound = 4;
  private boolean generating = false;

  /**
   * This method is the constructor for the GUI class. It takes in the title of
   * the window and the path to the image. It initializes all the GUI components
   * and organizes them in the correct component hierarchy.
   *
   * @param title    The title of the window.
   * @param filepath The path to the image to be spot-detected.
   */
  public GUI(String title, String filepath) {
    super(title);
    origImage = ImageLib.readImageFromFile(new File(filepath));

    //Setting window properties
    setSize(origImage.getWidth() + offset, origImage.getHeight());
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);
    setLayout(new FlowLayout());

    Container mainContainer = getContentPane();

    //Setting the image to be initially displayed
    imageLabel = new JLabel(); //using a JLabel icon to display the image
    setImage(origImage);

    //Image Panel
    JPanel imagePanel = new JPanel();
    imagePanel.setPreferredSize(new Dimension(origImage.getWidth(),
        origImage.getHeight()));
    mainContainer.add(imagePanel);
    imagePanel.add(imageLabel);

    //Control Panel
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new GridLayout(0, 1));
    controlPanel.setPreferredSize(new Dimension(offset, origImage.getHeight()));
    mainContainer.add(controlPanel);

    createControlComponents();
    createListeners();

    //Control Panel Sub-Panels
    JPanel modePanel = new JPanel();
    JPanel epsilonPanel = new JPanel();
    JPanel spotPanel = new JPanel();
    JPanel generatePanel = new JPanel();
    JPanel spotCountPanel = new JPanel();

    controlPanel.add(modePanel);
    controlPanel.add(epsilonPanel);
    controlPanel.add(spotPanel);
    controlPanel.add(generatePanel);
    controlPanel.add(spotCountPanel);

    //Adding components to respective panels
    modePanel.add(modeLabel);
    modePanel.add(modeSelect);

    epsilonPanel.add(epsilonLabel);
    epsilonPanel.add(epsilonTextField);
    epsilonPanel.add(epsilonSlider);

    spotPanel.add(lowerLabel);
    spotPanel.add(lowerBoundSelect);
    spotPanel.add(upperLabel);
    spotPanel.add(upperBoundSelect);

    generatePanel.add(generateButton);
    generatePanel.add(generatingLabel);

    spotCountPanel.add(spotCountLabel);

    pack();
    setVisible(true);
  }

  /**
   * This method initializes all the components stored in the control panel.
   */
  private void createControlComponents() {

    //Mode related components
    modeLabel = new JLabel("Mode:");
    modeSelect = new JComboBox<>(modes);
    modeSelect.setEnabled(false);

    //Epsilon related components
    epsilonLabel = new JLabel("Epsilon:");
    epsilonTextField = new JTextField("" + epsilon);
    epsilonTextField.setPreferredSize(new Dimension(100, 20));
    epsilonSlider = new JSlider(0, 255, epsilon);

    //Spot-detection bounds related components
    lowerLabel = new JLabel("Lower bound:");
    upperLabel = new JLabel("Upper bound:");
    lowerBoundSelect = new JComboBox<>(radii);
    upperBoundSelect = new JComboBox<>(radii);

    generateButton = new JButton("Generate");
    generatingLabel = new JLabel();

    spotCountLabel = new JLabel();

  }

  /**
   * This method adds various listeners to components of the GUI. These
   * listeners in turn will trigger various other methods in this class.
   * Notably: *A listener to change which mode of spot detection to have its
   * image displayed. *Listeners for the components which alter the parameters
   * for spot detection. *A listener for the "Generate Button" which triggers an
   * event to rerun the spot detection algorithm.
   */
  private void createListeners() {

    modeSelect.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        changeMode(modeSelect.getSelectedIndex());
      }
    });

    epsilonSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (!epsilonSlider.getValueIsAdjusting()) {
          changeEpsilon(epsilonSlider.getValue());
        }
      }
    });

    epsilonTextField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        textChanged();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        textChanged();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        textChanged();
      }

      public void textChanged() {
        try {
          changeEpsilon(Integer.parseInt("" + epsilonTextField.getText()));
        } catch (NumberFormatException ignored) {

        }
      }
    });

    lowerBoundSelect.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        changeLowerBound((Integer) lowerBoundSelect.getSelectedItem());
      }
    });

    upperBoundSelect.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        changeUpperBound((Integer) upperBoundSelect.getSelectedItem());
      }
    });

    generateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        //Queue up a new generate task
        Generator g = new Generator();
        g.start();
      }
    });
  }

  /**
   * This method changes the lowerBound to a new value specified. It also checks
   * whether the new lowerBound is less than or equal to the upperBound. If this
   * is not the case it sets the new lowerBound equal to the current
   * upperBound.
   *
   * @param value The new lower-bound value.
   */
  private void changeLowerBound(int value) {
    if (value > upperBound) {
      value = upperBound;
    }
    lowerBoundSelect.setSelectedItem(value);
    lowerBound = value;
  }

  /**
   * This method changes the upperBound to a new value specified. It also checks
   * whether the new upperBound is larger than or equal to the lowerBound. If
   * this is not the case it sets the new upperBound equal to the current
   * lowerBound.
   *
   * @param value The new upper-bound value.
   */
  private void changeUpperBound(int value) {
    if (value < lowerBound) {
      value = lowerBound;
    }
    upperBoundSelect.setSelectedItem(value);
    upperBound = value;
  }

  /**
   * This method changes the image that is displayed.
   *
   * @param selectedIndex The index of the new mode. Indices correspond to the
   *                      entries in the "modes" array: 0 - Original Image 1 -
   *                      Greyscale Image 2 - Noise-reduced Image 3 - Edge-map 4
   *                      - Spot-map
   */
  private void changeMode(int selectedIndex) {
    switch (selectedIndex) {
      case 0:
        setImage(origImage);
        break;
      case 1:
        setImage(greyScale);
        break;
      case 2:
        setImage(nr);
        break;
      case 3:
        setImage(ed);
        break;
      case 4:
        setImage(sd.getSpotMap());
        break;
      default:
        System.err.println("Code should not reach here");
        return;
    }
    mode = selectedIndex;
  }

  /**
   * This method changes the epsilon parameter used in edge-detection to a
   * specified value. It clamps the provided integer value between 0 and 255. It
   * also updates the various GUI components associated with setting the epsilon
   * value.
   *
   * @param value The new integer epsilon value.
   */
  private void changeEpsilon(int value) {
    if (value == epsilon) {
      return;
    }

    if (value > 255) {
      value = 255;
    } else if (value < 0) {
      value = 0;
    }
    epsilon = value;
    epsilonSlider.setValue(epsilon);
    if (!epsilonTextField.hasFocus()) {
      epsilonTextField.setText("" + epsilon);
    }
  }

  /**
   * This method changes the image displayed in the Image panel to the provided
   * image.
   *
   * @param image The BufferedImage to be displayed.
   */
  private void setImage(BufferedImage image) {
    ImageIcon icon = new ImageIcon(image);
    imageLabel.setIcon(icon);
  }

  /**
   * This class acts primarily as way to run the spot detection algorithm on a
   * different thread. The primary reason for this is so that the GUI does not
   * freeze while the images are being generated, and can be visually updated to
   * show that image generation is taking place. The generateImages() code has a
   * check to make sure only a single generation task can take place at once.
   */
  private class Generator extends Thread {

    /**
     * This method is the method that runs when a Generator instance has its
     * thread started.
     */
    public void run() {
      generateImages();
    }

    /**
     * This method runs the spot detection algorithm. It first disables UI
     * components, then runs the various stages of the spot detection (storing
     * the results). Then it re-enables the UI components.
     */
    private void generateImages() {
      if (generating) {
        return;
      }
      //disabling parts of UI and indicating generating status
      generating = true;
      generateButton.setEnabled(false);
      modeSelect.setEnabled(false);
      generatingLabel.setText(genMessage);
      spotCountLabel.setText("");
      //spot detection
      greyScale = ImageLib.greyscale(origImage);
      nr = ImageLib.reduceNoise(origImage);
      ed = ImageLib.detectEdges(origImage, epsilon);
      sd = ImageLib.detectSpots(origImage, epsilon, lowerBound, upperBound);
      //re-enabling parts of UI and indicating final spot count
      generateButton.setEnabled(true);
      changeMode(mode);
      modeSelect.setEnabled(true);
      generatingLabel.setText(doneMessage);
      spotCountLabel.setText("Final spot count: " + sd.getSpotCount());
      generating = false;

    }
  }
}
