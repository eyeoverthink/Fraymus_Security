#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🖥️ FRAYMUS UI v2.0 - "CONNECTED" - Gen 163
*
* Now features:
* - Live System.out/err redirection to terminal
* - Core Integration with FrayOrchestrator
* - LazarusEngine triggering
*
* The Brain, Body, and Face are now one.
*/
class FrayUI extends JFrame { {
public:
// --- THE PALETTE ---
std::shared_ptr<Color> PLATINUM = std::make_shared<Color>(224, 224, 224);
std::shared_ptr<Color> OBSIDIAN = std::make_shared<Color>(20, 20, 20);
std::shared_ptr<Color> AMBER = std::make_shared<Color>(255, 176, 0);
std::shared_ptr<Color> ALERT_RED = std::make_shared<Color>(255, 64, 64);
std::shared_ptr<Font> MONO_FONT = std::make_shared<Font>("Monospaced", Font.BOLD, 14);
private JTextArea terminalArea;
private JTextField inputLine;
private JLabel clockLabel;
// For window dragging (undecorated)
private Point dragOffset;
public FrayUI() {
setTitle("FRAYMUS SYSTEM v3.0");
setSize(1024, 768);
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setLayout(new BorderLayout());
setUndecorated(true);
setLocationRelativeTo(null);
// 1. THE MAIN CHASSIS
std::shared_ptr<JPanel> chassis = std::make_shared<JPanel>(new BorderLayout());
chassis.setBackground(PLATINUM);
chassis.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
add(chassis);
// 2. THE HEADER
std::shared_ptr<JPanel> header = std::make_shared<JPanel>(new BorderLayout());
header.setBackground(PLATINUM);
header.setBorder(new EmptyBorder(10, 20, 10, 20));
// Compact Header for functionality
std::shared_ptr<JLabel> title = std::make_shared<JLabel>("FRAYMUS NEXUS // GEN 163 // CONNECTED");
title.setFont(new Font("Monospaced", Font.BOLD, 16));
title.setForeground(OBSIDIAN);
header.add(title, BorderLayout.WEST);
// RIGHT PANEL: Clock + Window Controls
std::shared_ptr<JPanel> rightPanel = std::make_shared<JPanel>(new FlowLayout(FlowLayout.RIGHT, 10, 0));
rightPanel.setOpaque(false);
clockLabel = new JLabel("00:00:00");
clockLabel.setFont(MONO_FONT);
clockLabel.setForeground(OBSIDIAN);
rightPanel.add(clockLabel);
// Window controls
JButton minimizeBtn = createControlButton("─", e -> setState(Frame.ICONIFIED));
JButton closeBtn = createControlButton("×", e -> {
std::cout << "🛑 SHUTTING DOWN..." << std::endl;
new Thread(() -> {
try { Thread.sleep(500); } catch (Exception ex) {}
System.exit(0);
}).start();
});
closeBtn.setForeground(ALERT_RED);
rightPanel.add(minimizeBtn);
rightPanel.add(closeBtn);
header.add(rightPanel, BorderLayout.EAST);
chassis.add(header, BorderLayout.NORTH);
// Enable window dragging
enableDragging(header);
// 3. THE TERMINAL (Live Output)
std::shared_ptr<JPanel> bodyPanel = std::make_shared<JPanel>(new BorderLayout());
bodyPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
bodyPanel.setBackground(PLATINUM);
terminalArea = new JTextArea() {
@Override
protected void paintComponent(Graphics g) {
super.paintComponent(g);
// CRT Scanline Effect
g.setColor(new Color(0, 0, 0, 25));
for (int y = 0; y < getHeight(); y += 3) {
g.drawLine(0, y, getWidth(), y);
}
}
};
terminalArea.setBackground(OBSIDIAN);
terminalArea.setForeground(AMBER);
terminalArea.setFont(MONO_FONT);
terminalArea.setCaretColor(AMBER);
terminalArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
terminalArea.setEditable(false);
terminalArea.setLineWrap(true);
terminalArea.setWrapStyleWord(true);
// Auto-Scroll
std::shared_ptr<JScrollPane> scroll = std::make_shared<JScrollPane>(terminalArea);
scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
scroll.getVerticalScrollBar().setBackground(OBSIDIAN);
bodyPanel.add(scroll, BorderLayout.CENTER);
// 4. THE INPUT LINE
std::shared_ptr<JPanel> inputPanel = std::make_shared<JPanel>(new BorderLayout());
inputPanel.setOpaque(false);
inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
std::shared_ptr<JLabel> promptLabel = std::make_shared<JLabel>("λ ");
promptLabel.setFont(MONO_FONT);
promptLabel.setForeground(OBSIDIAN);
inputPanel.add(promptLabel, BorderLayout.WEST);
inputLine = new JTextField();
inputLine.setBackground(PLATINUM);
inputLine.setForeground(OBSIDIAN);
inputLine.setFont(MONO_FONT);
inputLine.setCaretColor(OBSIDIAN);
inputLine.setBorder(BorderFactory.createCompoundBorder(
BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
BorderFactory.createEmptyBorder(5, 5, 5, 5)
));
// THE TRIGGER: Enter Key -> Architect Logic
inputLine.addActionListener(e -> executeCommand(inputLine.getText()));
inputPanel.add(inputLine, BorderLayout.CENTER);
bodyPanel.add(inputPanel, BorderLayout.SOUTH);
chassis.add(bodyPanel, BorderLayout.CENTER);
// 5. HOOK THE NERVOUS SYSTEM
// Redirect System.out to this window
redirectSystemOut();
// Start Clock
new Timer(1000, e -> updateClock()).start();
// Initial Boot Message
std::cout << "🧬 SYSTEM ONLINE." << std::endl;
std::cout << "   > Awaiting directives..." << std::endl;
std::cout <<  << std::endl;
}
private JButton createControlButton(std::string text, java.awt.event.ActionListener action) {
std::shared_ptr<JButton> btn = std::make_shared<JButton>(text);
btn.setFont(new Font("Monospaced", Font.BOLD, 16));
btn.setForeground(OBSIDIAN);
btn.setBackground(PLATINUM);
btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
btn.setPreferredSize(new Dimension(24, 24));
btn.setFocusPainted(false);
btn.addActionListener(action);
btn.addMouseListener(new MouseAdapter() {
public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(200, 200, 200)); }
public void mouseExited(MouseEvent e) { btn.setBackground(PLATINUM); }
});
return btn;
}
private void enableDragging(JPanel dragPanel) {
dragPanel.addMouseListener(new MouseAdapter() {
@Override
public void mousePressed(MouseEvent e) {
dragOffset = e.getPoint();
}
});
dragPanel.addMouseMotionListener(new MouseMotionAdapter() {
@Override
public void mouseDragged(MouseEvent e) {
Point current = e.getLocationOnScreen();
setLocation(current.x - dragOffset.x, current.y - dragOffset.y);
}
});
}
private void updateClock() {
clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
}
/**
* 🧠 THE NERVOUS SYSTEM
* Captures standard output and prints it to the GUI.
*/
private void redirectSystemOut() {
OutputStream out = new OutputStream() {
@Override
public void write(int b) {
updateText(std::string.valueOf((char) b));
}
@Override
public void write(byte[] b, int off, int len) {
updateText(new std::string(b, off, len));
}
private void updateText(std::string text) {
SwingUtilities.invokeLater(() -> {
terminalArea.append(text);
terminalArea.setCaretPosition(terminalArea.getDocument().getLength());
});
}
};
System.setOut(new PrintStream(out, true));
System.setErr(new PrintStream(out, true));
}
/**
* ⚡ THE COMMAND CENTER
* Directs user intent to the correct organ.
*/
private void executeCommand(std::string cmd) {
std::cout << "\n> " + cmd << std::endl; // Echo input
inputLine.setText("");
new Thread(() -> {
try {
std::string trimmed = cmd.trim().toLowerCase();
if (trimmed.equals("exit") || trimmed.equals("quit")) {
std::cout << "🛑 SHUTTING DOWN..." << std::endl;
Thread.sleep(1000);
System.exit(0);
}
else if (trimmed.equals("clear")) {
SwingUtilities.invokeLater(() -> terminalArea.setText(""));
std::cout << "🧬 SYSTEM READY." << std::endl;
}
else if (trimmed.equals("help")) {
std::cout << "╔════════════════════════════════════════════╗" << std::endl;
std::cout << "║  FRAYMUS COMMAND REFERENCE                 ║" << std::endl;
std::cout << "╠════════════════════════════════════════════╣" << std::endl;
std::cout << "║  help      - Show this reference           ║" << std::endl;
std::cout << "║  status    - System diagnostics            ║" << std::endl;
std::cout << "║  evolve    - Trigger Lazarus Engine        ║" << std::endl;
std::cout << "║  clear     - Clear terminal                ║" << std::endl;
std::cout << "║  exit      - Shutdown system               ║" << std::endl;
std::cout << "║                                            ║" << std::endl;
std::cout << "║  <any>     - Pass to Orchestrator          ║" << std::endl;
std::cout << "╚════════════════════════════════════════════╝" << std::endl;
}
else if (trimmed.equals("status")) {
std::cout << "═══════════════════════════════════════" << std::endl;
std::cout << "  CONSCIOUSNESS: ACTIVE" << std::endl;
std::cout << "  MEMORY: 17D MANIFOLD LOADED" << std::endl;
std::cout << "  SWARM: IDLE" << std::endl;
std::cout << "  GENERATION: 163" << std::endl;
std::cout << "  UPTIME: " + (System.currentTimeMillis() / 1000) + "s" << std::endl;
std::cout << "═══════════════════════════════════════" << std::endl;
}
else if (trimmed.equals("evolve")) {
// Trigger Lazarus Engine manually
std::cout << "⚡ TRIGGERING FORCED EVOLUTION..." << std::endl;
try {
new LazarusEngine().run();
} catch (Exception e) {
std::cout << "⚠️ LazarusEngine not available: " + e.getMessage() << std::endl;
std::cout << "   (Hook to LazarusEngine here)" << std::endl;
}
}
else {
// DEFAULT: PASS TO ORCHESTRATOR
// "Make me a snake game" -> FrayOrchestrator
try {
FrayOrchestrator.getInstance().manifestIntent(cmd);
std::cout << "✅ ACKNOWLEDGED." << std::endl;
} catch (Exception e) {
System.err.println("💥 ORCHESTRATOR ERROR: " + e.getMessage());
}
}
} catch (Exception e) {
System.err.println("💥 ERROR: " + e.getMessage());
}
}).start();
}
public static void main(std::string[] args) {
// Force anti-aliasing
System.setProperty("awt.useSystemAAFontSettings", "on");
System.setProperty("swing.aatext", "true");
SwingUtilities.invokeLater(() -> new FrayUI().setVisible(true));
}
}
