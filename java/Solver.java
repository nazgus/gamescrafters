import java.util.ArrayList;
import java.util.TreeMap;
import java.lang.reflect.*;
import java.util.Map.Entry;
import java.util.Set;

public class Solver {
    private TreeMap<Integer, PosValue> solvedPos;
    private Game game;
    private boolean output;

    public Solver(Game game, boolean output) {
        this.game = game;
        this.solvedPos = new TreeMap<Integer, PosValue>();
        this.output = output;
    }

    public TreeMap<Integer, PosValue> getSolvedPos() { return this.solvedPos; }

    public PosValue solve(int pos) {
        PosValue pVal = game.primitive(pos);
        if (!pVal.equals("undecided")) {
            if (!this.solvedPos.containsKey(pos)) {
                this.solvedPos.put(pos, pVal);
            }
            return pVal;
        }

        ArrayList<Integer> moves = game.generateMoves(pos);
        int numWinningChildren = 0;
        int minRemoteness = Integer.MAX_VALUE;
        for (int move : moves) {
            int newPos = game.doMove(pos, move);
            PosValue outcome = (this.solvedPos.containsKey(newPos)) ? this.solvedPos.get(newPos) : this.solve(newPos);

            if (outcome.equals("losing")) {
                pVal = new PosValue("winning", outcome.getRemoteness() + 1, move);
                this.solvedPos.put(pos, pVal);
                return pVal;
            } else if (outcome.equals("winning")) {
                numWinningChildren++;
            }
            minRemoteness = Math.min(minRemoteness, outcome.getRemoteness());
        }

        String res = (numWinningChildren == moves.size()) ? "losing" : "tie";
        pVal = new PosValue(res, minRemoteness + 1);
        if (!this.solvedPos.containsKey(pos)) {
            this.solvedPos.put(pos, pVal);
        }
        return pVal;
    }

    public static void main(String[] args) {
        Game game;
        try {
            if (args.length < 1 || args.length > 2) {
                System.out.println("Must pass in a game class name.");
                System.exit(-1);
            }
            if (args.length == 1) {
                game = (Game) Class.forName(args[0]).newInstance();
            } else if (args.length == 2) {
                Class<?> c = Class.forName(args[0]);
                Constructor<?> con = c.getConstructor(int.class);
                game = (Game) con.newInstance(Integer.parseInt(args[1]));
            } else {
                game = new TenToZero(); // Never executed
            }
        } catch (NumberFormatException nfe) {
            game = new TenToZero();
            System.out.printf("The second argument must be an integer. Error: %s\n", nfe);
            System.exit(1);
        } catch (Exception e) {
            game = new TenToZero(); // Complains if I don't have it...
            System.out.println(e);
            System.exit(1);
        }

        Solver solver = new Solver(game, true);
        solver.solve(game.getInitialPos());
        Set<Entry<Integer, PosValue>> entrySet = solver.solvedPos.entrySet();
        if (solver.output) {
            String output;
            PosValue pVal;
            for (Entry<Integer, PosValue> entry : entrySet) {
                pVal = entry.getValue();
                if (pVal.equals("winning") && pVal.getRemoteness() != 0) {
                    output = String.format("%s is a %s position with a remoteness of %d by playing '%d'.\n", game.posToString(entry.getKey()), pVal.getValue(), pVal.getRemoteness(), pVal.getWinningMove());
                } else {
                    output = String.format("%s is a %s position with a remoteness of %d.\n", game.posToString(entry.getKey()), pVal.getValue(), pVal.getRemoteness());
                }
                System.out.print(output);
            }
        }
    }
}
