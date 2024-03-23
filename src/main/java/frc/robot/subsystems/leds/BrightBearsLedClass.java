package frc.robot.subsystems.leds;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LedConstants;

public class BrightBearsLedClass extends SubsystemBase {
    public AddressableLED m_Led = new AddressableLED(LedConstants.LedID);
    private static AddressableLEDBuffer ledBuffer = new AddressableLEDBuffer(LedConstants.totalLength);
    private final Timer strobeTimer = new Timer();
    private final Timer zipTimer = new Timer();
    private final Timer fillTimer = new Timer();

    private static int brightnessLimit = 100;
    private int m_rainbowFirstPixelHue;
    private int m_range;
    private int zipIncrease;
    private int start;
    private int end;
    private static final double fadeExponent = 0.4;
    public boolean on = true;

    public BrightBearsLedClass() {
        m_Led.setLength(ledBuffer.getLength());
        m_Led.setData(ledBuffer);
        m_Led.start();
        strobeTimer.start();
        zipTimer.start();
        fillTimer.start();
    }

    @Override
    public void periodic() {
        m_Led.setData(ledBuffer);
    }

    /**
     * Sets the strip to a solid. Currently only a single color
     * 
     * @param section is the range you want to add the effect
     * @param color   is the color you want the LEDs to be
     */
    public void solid(Section section, LedColor color) {
        for (var i = section.start(); i < section.end(); i++) {
            final var hue = color.hues();
            final var value = color.value();
            // Sets the specified LED to the HSV values for the preferred color
            ledBuffer.setHSV(i, hue, 255, value);
        }
    }

    /**
     * Sets the strip to a solid. Currently only a single color
     * 
     * @param section is the range you want to add the effect
     * @param color1  is the color you want the LEDs to be
     * @param color2  is the second color you want to display
     */
    public void solidTwoColor(Section section, LedColor color1, LedColor color2) {

        for (var i = section.start(); i < section.end(); i++) {
            if (i % 2 == 0) {
                final var hue = color1.hues();
                final var value = color1.value();
                // Sets the specified LED to the HSV values for the preferred color
                ledBuffer.setHSV(i, hue, 255, value);
            } else {
                final var hue = color2.hues();
                final var value = color2.value();
                // Sets the specified LED to the HSV values for the preferred color
                ledBuffer.setHSV(i, hue, 255, value);
            }
        }
    }

    /**
     * Sets the strip to a solid color based on the hue given instead of a preset
     * color
     * 
     * @param section is the range you want to add the effect
     * @param hue     is the hue you want to send to the light
     */
    public void colorTest(Section section, double hue) {
        for (var i = section.start(); i < section.end(); i++) {
            final var value = brightnessLimit;
            // Sets the specified LED to the HSV values for the preferred color
            ledBuffer.setHSV(i, (int) hue, 255, value);
        }
    }

    /**
     * Sets the strip to a rainbow pattern that moves along the light strand
     * 
     * @param section is the range you want to add the effect
     * @param speed   is the speed you want the rainbow to run down the strip - 5 is
     *                recommended for long distance, 3 is recommended for short
     *                distances
     */
    public void rainbow(Section section, int speed) {
        // For designate range
        for (var i = section.start(); i < section.end(); i++) {
            // Calculate the hue - hue is easier for rainbows because the color
            // shape is a circle so only one value needs to precess
            final var hue = (m_rainbowFirstPixelHue + (i * 180 / section.end())) % 180;
            // Set the value
            ledBuffer.setHSV(i, hue, 255, brightnessLimit);
        }
        // Increase by to make the rainbow "move"
        m_rainbowFirstPixelHue += speed;
        // Check bounds
        m_rainbowFirstPixelHue %= 180;
    }

    /**
     * Fades between the colors designated - on the hue scale
     * 
     * @param section     is the range you want to add the effect
     * @param color1      is the base color
     * @param color2      is the color to fade in and out of
     * @param cycleLength is the frequency you fade the color by the pixels
     * @param duration    is the time it takes to cycle back to the original color
     */
    public void fade(Section section, LedColor color1, LedColor color2, int cycleLength, double duration) {
        double x = (1 - ((Timer.getFPGATimestamp() % duration) / duration)) * 2.0 * Math.PI;
        double xDiffPerLed = (2.0 * Math.PI) / cycleLength;
        final var value = color1.value();
        for (int i = section.start(); i < section.end(); i++) {
            x += xDiffPerLed;
            if (i >= section.start()) {
                double ratio = (Math.pow(Math.sin(x), fadeExponent) + 1.0) / 2.0;
                if (Double.isNaN(ratio)) {
                    ratio = (-Math.pow(Math.sin(x + Math.PI), fadeExponent) + 1.0) / 2.0;
                }
                if (Double.isNaN(ratio)) {
                    ratio = 0.5;
                }

                int outputColor = (int) Math.round((color1.hues() * (1 - ratio)) + (color2.hues() * ratio));
                ledBuffer.setHSV(i, outputColor, 255, value);
            }
        }
    }

    /**
     * Pulses the lights to give them a breathing effect.
     * 
     * @param section  is the range you want to add the effect
     * @param color    is the color that with breath
     * @param duration is the time it takes to go through 1 cycle
     */
    public void breath(Section section, LedColor color1, double duration) {
        double x = ((Timer.getFPGATimestamp() % duration) / duration) * 2.0 * Math.PI;
        double ratio = (Math.sin(x) + 1.0) / 2.0;
        LedColor color2 = LedColor.BLACK;

        for (int i = section.start(); i < section.end(); i++) {
            final var value = (color1.value() * (1 - ratio)) + (color2.value() * ratio);
            ledBuffer.setHSV(i, color1.hues(), 255, (int) value);
        }
    }

    /**
     * Flashes the LEDs on and off at the designated speed
     * 
     * @param section  is the range you want to add the effect
     * @param color    is the color that will flash
     * @param duration is the time between each flash
     */
    public void strobe(Section section, LedColor color, double duration) {
        strobe(section, color, LedColor.BLACK, duration);
    }

    /**
     * Flashes the LEDs between two colors at the designated speed
     * 
     * @param section  is the range you want to add the effect
     * @param color1   is the base color
     * @param color2   is the secondary color
     * @param duration is the time between each flash
     */
    public void strobe(Section section, LedColor color1, LedColor color2, double duration) {
        if (!strobeTimer.advanceIfElapsed(duration / 2))
            return;

        if (!on) {
            solid(section, color1);
            on = true;
        } else {
            solid(section, color2);
            on = false;
        }
    }

    /**
     * Fills the section of LEDs with the color incrementally
     * 
     * @param section   is the range you want to add the effect
     * @param color     is the color you want to fill
     * @param increment is how many LEDs you want to fill per cycle
     * @param duration  is the length of time it takes to go through the whole
     *                  section. Currently restricted at greater than 2
     * @param inverse   is the direction you would like to go - false starts at the
     *                  RoboRio, true goes towards the RoboRio
     */
    public void fill(Section section, LedColor color, int increment, double duration, boolean inverse) {
        LedColor color2 = LedColor.BLACK;
        for (int i = section.start(); i < section.end(); i++) {
            if (!inverse) {
                if (i <= section.start() + m_range) {
                    ledBuffer.setHSV(i, color.hues(), 255, color.value());
                } else {
                    ledBuffer.setHSV(i, color2.hues(), 255, color2.value());
                }
            } else {
                if (i >= section.end() - m_range) {
                    ledBuffer.setHSV(i, color.hues(), 255, color.value());
                } else {
                    ledBuffer.setHSV(i, color2.hues(), 255, color2.value());
                }
            }
        }
        // Only pass if the time calculated has passed
        double speed = increment * (duration / (section.end() - section.start()));
        if (!fillTimer.advanceIfElapsed(speed))
            return;

        // increase to fill the strip
        m_range += increment;
        // check bounds
        m_range %= section.end() - section.start();
    }

    /**
     * Moves a smaller section of leds through the entire section
     * 
     * @param section   is the range you want to add the effect
     * @param color     is the color you want to display
     * @param length    is the length of the strip of activated lights
     * @param increment is how many LEDs you want to add per cycle
     * @param duration  is the length of time it takes to go through the entire
     *                  section. Currently restricted at greater than 2
     * @param inverse   is the direction you would like to go - false starts at the
     *                  RoboRio, true goes towards the RoboRio
     */
    public void zip(Section section, LedColor color, int length, int increment, double duration, boolean inverse) {
        LedColor color2 = LedColor.BLACK;
        if (!inverse) {
            start = section.start() + zipIncrease;
            end = start + length;
        } else {
            start = section.end() - zipIncrease;
            end = start - length;
        }

        for (int i = section.start(); i < section.end(); i++) {
            if (!inverse) {
                if (i > start && i <= end) {
                    ledBuffer.setHSV(i, color.hues(), 255, color.value());
                } else {
                    ledBuffer.setHSV(i, color2.hues(), 255, color2.value());
                }
            } else {
                if (i < start && i >= end) {
                    ledBuffer.setHSV(i, color.hues(), 255, color.value());
                } else {
                    ledBuffer.setHSV(i, color2.hues(), 255, color2.value());
                }
            }
        }

        // Only pass if the time calculated has passed
        double speed = increment * (duration / (section.end() - section.start()));
        if (!fillTimer.advanceIfElapsed(speed))
            return;

        // increase to move the strip
        zipIncrease += increment;
        // check bounds
        zipIncrease %= section.end() - section.start() - length;
    }

    public static enum LedColor {
        RED,
        ORANGE,
        YELLOW,
        GREEN,
        AQUA,
        LIGHT_BLUE,
        BLUE,
        PURPLE,
        PINK,
        MAGENTA,
        BLACK;

        public int hues() {
            switch (this) { // Values are divided by 2 because color pickers like Google are x/360, whereas
                            // WPILib is x/180
                case RED:
                    return 0;
                case ORANGE:
                    return 10 / 2;
                case YELLOW:
                    return 40 / 2;
                case GREEN:
                    return 110 / 2;
                case AQUA:
                    return 130 / 2;
                case LIGHT_BLUE:
                    return 180 / 2;
                case BLUE:
                    return 240 / 2;
                case PURPLE:
                    return 260 / 2;
                case PINK:
                    return 336 / 2;
                case MAGENTA:
                    return 356 / 2;
                case BLACK:
                    return 0;
                default:
                    return 0;
            }
        }

        public int value() {
            switch (this) {
                case RED:
                    return brightnessLimit;
                case ORANGE:
                    return brightnessLimit;
                case YELLOW:
                    return brightnessLimit;
                case GREEN:
                    return brightnessLimit;
                case AQUA:
                    return brightnessLimit;
                case LIGHT_BLUE:
                    return brightnessLimit;
                case BLUE:
                    return brightnessLimit;
                case PURPLE:
                    return brightnessLimit;
                case PINK:
                    return brightnessLimit;
                case BLACK:
                    return 0;
                default:
                    return brightnessLimit;
            }
        }
    }

    public static enum Section {
        LEFT,
        RIGHT,
        TOP,
        LAUNCHER,
        FULL,
        UNDERGLOW;

        public int start() {
            switch (this) {
                case UNDERGLOW:
                    return 0;
                case LEFT:
                    return LedConstants.underLength;
                case TOP:
                    return LedConstants.underLength + LedConstants.leftLength;
                case RIGHT:
                    return LedConstants.underLength + LedConstants.leftLength + LedConstants.topLength;
                case LAUNCHER:
                    return LedConstants.underLength;
                case FULL:
                    return 0;
                default:
                    return 0;
            }
        }

        public int end() {
            switch (this) {
                case UNDERGLOW:
                    return LedConstants.underLength;
                case LEFT:
                    return LedConstants.underLength + LedConstants.leftLength;
                case TOP:
                    return LedConstants.underLength + LedConstants.leftLength + LedConstants.topLength;
                case RIGHT:
                    return LedConstants.totalLength;
                case LAUNCHER:
                    return LedConstants.totalLength;
                case FULL:
                    return LedConstants.totalLength;
                default:
                    return LedConstants.totalLength;
            }
        }
    }
}
