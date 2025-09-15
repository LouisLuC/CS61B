package gh2;

import edu.princeton.cs.algs4.Genome;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    private static final String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    private static final GuitarString[] notes = new GuitarString[GuitarHero.keyboard.length()];

    /* create 37 guitar strings */
    private static void guitarStringInit() {
        for (int i = 0; i < GuitarHero.notes.length; i++) {
            notes[i] = new GuitarString(440.0 * Math.pow(2, (i - 24) / 12.0));
        }
    }

    private static double compose() {
        double sample = 0.0;
        for (int i = 0; i < GuitarHero.notes.length; i++) {
            sample+=GuitarHero.notes[i].sample();
        }
        return sample;
    };

    private static void notesTic() {
        for (int i = 0; i < GuitarHero.notes.length; i++) {
            GuitarHero.notes[i].tic();
        }
    }


    // public static final double CONCERT_C = CONCERT_A * Math.pow(2, 3.0 / 12.0);

    public static void main(String[] args) {
        guitarStringInit();
        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int keyIndex = keyboard.indexOf(key);
                System.out.println("The key user pressed is " + key + ", the index is " + keyIndex);
                if (keyIndex == -1)
                    continue;
                GuitarString note = notes[keyIndex];
                note.pluck();
                /*
                if (key == 'a') {
                    stringA.pluck();
                } else if (key == 'c') {
                    stringC.pluck();
                }
                 */
            }

            /* compute the superposition of samples */
            // double sample = stringA.sample() + stringC.sample();
            double sample = compose();

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            // stringA.tic();
            // stringC.tic();
            notesTic();
        }
    }
}

