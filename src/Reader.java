import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Reader can be used to read a line of text in an image. If no language is
 * provided, the default is English. If no background color is provided, the
 * default is white (meaning all non-white pixels in the image are considered "text").
 */

public class Reader {
	
	/** 
	 * Every character in the given images is boxed in the minimum area square
	 * that fully contains it (extra pixels of color "background" are added as 
	 * necessary to fix the ratio) and then that square is scaled down to a square
	 * of dimensions SCALE_SIZE x SCALE_SIZE pixels prior to text recognition.
	 */
	private static final int SCALE_SIZE = 9;
	
	/** The language that is read off of the text in the image. */
	private String language;
	
	/** 
	 * The color of the background behind the text in the image
	 *  (all pixels not this color are considered "text"). 
	 */
	private Color background;
	
	/** 
	 * The mapping of all character patterns to the respective character.
	 *  For example, {(4, 0), (4, 1), (4, 2), (4, 3), (4, 4),
	 *  			  (4, 5), (4, 6), (4, 7), (4, 8)} --> 'l' 
	 */
	private Map<HashSet<Point>, Character> characters;	
	
	/**
	 * Constructs a new Reader object with language English and background color of white.
	 */
	public Reader() {
		this("English", Color.WHITE);
	}
	
	/**
	 * Constructs a new Reader object with the given text language and background color.
	 * 
	 * @param language The language of the text to be read.
	 * @param background The color of the "background" (all non-text pixels).
	 * @throws IllegalArgumentException if language is not one of the following
	 * 		   languages: English.
	 * @throws NullPointerException if either argument is null.
	 */
	public Reader(String language, Color background) {
		if (language == null) {
			throw new NullPointerException("Language cannot be null...");
		}
		if (background == null) {
			throw new NullPointerException("Background color cannot be null...");
		}
		try {
			this.characters = loadCharacters(language);
			this.language = language;
			this.background = background;
		} catch (IOException e) {
			throw new IllegalArgumentException("Illegal language: " + language);
		}
	}
	
	/**
	 * Sets the language of this reader to <i>language</i>.
	 * 
	 * @param language The language of the text to be read.
	 * @throws IllegalArgumentException if language is not one of the following
	 * 		   languages: English.
	 * @throws NullPointerException if language is null.
	 */
	public void setLanguage(String language) {
		if (language == null) {
			throw new NullPointerException("Language cannot be null...");
		}
		try {
			this.characters = loadCharacters(language);
			this.language = language;
		} catch (IOException e) {
			throw new IllegalArgumentException("Illegal language: " + language);
		}
	}
	
	/**
	 * Returns the current language of this Reader.
	 * 
	 * @return the current language of the Reader.
	 */
	public String getLanguage() {
		return this.language;
	}
	
	/**
	 * Sets the background color of this reader to <i>background</i>.
	 * 
	 * @throws NullPointerException if background is null.
	 */
	public void setBackground(Color background) {
		if (background == null) {
			throw new NullPointerException("Background color cannot be null...");
		}
		this.background = background;
	}
	
	/**
	 * Returns the current background color of this Reader.
	 * 
	 * @return the background color of this Reader.
	 */
	public Color getBackground() {
		return this.background;
	}
	
	/**
	 * Returns the text pictured in the image <i>f</i>.
	 * 
	 * @param f The file to read text from.
	 * @return The text in the picture.
	 * @throws NullPointerEception if f is null.
	 * @throws IllegalArgumentException if f is invalid.
	 */
	public String read(File f) {
		if (f == null) {
			throw new NullPointerException("Filename cannot be null...");
		}
		try {
			// find pixel indices of left and right edges of each character in text
			BufferedImage img = ImageIO.read(f);
			List<Integer> charBreakpoints = new ArrayList<>();
			// FIXME: Add support for multiple chars in an image
			charBreakpoints.add(0);
			charBreakpoints.add(img.getWidth());
			
			// recognize each character in text individually
			String result = "";
			for (int i = 0; i < charBreakpoints.size() - 1; i++) {
				result += readChar(img, charBreakpoints.get(i), charBreakpoints.get(i + 1));
			}
			return result;
		} catch (IOException e) {
			throw new IllegalArgumentException("Illegal file: " + f);
		}
	}
	
	/**
	 * Returns the single character pictured in the portion of img with pixels
	 * with x coordinates between lo and hi (inclusive). If the pixels in the
	 * rectangle (lo, 0) (hi, 0) (hi, img.getHeight()), (lo, img.getHeight()) do
	 * not represent a single character, the result is undefined.
	 * 
	 * @param img The BufferedImage from which to read.
	 * @param lo The leftmost pixel from which to read.
	 * @param hi The rightmost pixel to read until.
	 * @requires non-null parameters and values of lo and hi in [0, img.getWidth() - 1].
	 * @return The character written in the region of <i>img</i> between lo and hi.
	 */
	private char readChar(BufferedImage img, int lo, int hi) {
		return 'a';
	}
	
	/**
	 * Returns a map of pixel patterns (Points on the square of dimension
	 * SCALE_SIZE x SCALE_SIZE) to characters (based on the given language file).
	 * 
	 * The language file should be formatted as follows: tab-separated pixel
	 * coordinates (each delimited by ', ') followed by a tab, followed by a
	 * single character representing the character that the pixel pattern
	 * containing all those coordinates represents (one character per line).
	 * 
	 * Example line:
	 * 4, 0	4, 1	4, 2	4, 3	4, 4	4, 5	4, 6	4, 7	4, 8	1
	 * 
	 * @param language The language to load a map of character patterns for.
	 * @requires a non-null language; a properly formatted language file "data/LANGUAGE.txt":
	 * @return The map of pixels patterns to characters.
	 * @throws IOException if the file data/<i>language</i>.txt cannot be found.
	 */
	private Map<HashSet<Point>, Character> loadCharacters(String language) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("data/" + language + ".txt"));
		try {
			Map<HashSet<Point>, Character> characters = new HashMap<>();
			String inputLine = reader.readLine();
			while (inputLine != null) {
				// parse the data
				String[] tokens = inputLine.split("\t");
				assert tokens.length > 0: "Bad line " + inputLine + "!";
				// first (length - 1) entries are points, last is actual character
				Character c = tokens[tokens.length - 1].charAt(0);
				for (int i = 0; i < tokens.length - 1; i++) {
					HashSet<Point> points = new HashSet<>();
					String[] pointTokens = tokens[i].split(", ");
					assert pointTokens.length == 2: "Bad line " + inputLine + "!";
					int x = Integer.parseInt(pointTokens[0]);
					int y = Integer.parseInt(pointTokens[1]);
					Point p = new Point(x, y);
					points.add(p);
					characters.put(points, c);
				}
				inputLine = reader.readLine();
			}
			return characters;
		} finally {
			reader.close();
		}
	}
}