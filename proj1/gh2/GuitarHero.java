package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    private static final String KEYBOARD = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    private static final GuitarString[] NOTES = new GuitarString[GuitarHero.KEYBOARD.length()];

    /* create 37 guitar strings */
    private static void guitarStringInit() {
        for (int i = 0; i < GuitarHero.NOTES.length; i++) {
            NOTES[i] = new GuitarString(440.0 * Math.pow(2, (i - 24) / 12.0));
        }
    }

    private static double compose() {
        double sample = 0.0;
        for (int i = 0; i < GuitarHero.NOTES.length; i++) {
            sample += GuitarHero.NOTES[i].sample();
        }
        return sample;
    }

    private static void notesTic() {
        for (int i = 0; i < GuitarHero.NOTES.length; i++) {
            GuitarHero.NOTES[i].tic();
        }
    }


    // public static final double CONCERT_C = CONCERT_A * Math.pow(2, 3.0 / 12.0);

    public static void main(String[] args) {
        guitarStringInit();
        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int keyIndex = KEYBOARD.indexOf(key);
                System.out.println("The key user pressed is " + key + ", the index is " + keyIndex);
                if (keyIndex == -1) {
                    continue;
                }
                GuitarString note = NOTES[keyIndex];
                note.pluck();
            }

            /* compute the superposition of samples */
            // double sample = stringA.sample() + stringC.sample();
            double sample = compose();

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            notesTic();
        }
    }
}

