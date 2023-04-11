//package com.cz4046;

import java.util.*;

public class ThreePrisonersDilemma_clean {

	/*
	 This Java program models the two-player Prisoner's Dilemma game.
	 We use the integer "0" to represent cooperation, and "1" to represent
	 defection.

	 Recall that in the 2-players dilemma, U(DC) > U(CC) > U(DD) > U(CD), where
	 we give the payoff for the first player in the list. We want the three-player game
	 to resemble the 2-player game whenever one player's response is fixed, and we
	 also want symmetry, so U(CCD) = U(CDC) etc. This gives the unique ordering

	 U(DCC) > U(CCC) > U(DDC) > U(CDC) > U(DDD) > U(CDD)

	 The payoffs for player 1 are given by the following matrix: */

    static int[][][] payoff = {
            {{6,3},  //payoffs when first and second players cooperate
            {3,0}},  //payoffs when first player coops, second defects
            {{8,5},  //payoffs when first player defects, second coops
            {5,2}}}; //payoffs when first and second players defect

	/*
	 So payoff[i][j][k] represents the payoff to player 1 when the first
	 player's action is i, the second player's action is j, and the
	 third player's action is k.

	 In this simulation, triples of players will play each other repeatedly in a
	 'match'. A match consists of about 100 rounds, and your score from that match
	 is the average of the payoffs from each round of that match. For each round, your
	 strategy is given a list of the previous plays (so you can remember what your
	 opponent did) and must compute the next action.  */


    abstract class Player {
        // This procedure takes in the number of rounds elapsed so far (n), and
        // the previous plays in the match, and returns the appropriate action.
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            throw new RuntimeException("You need to override the selectAction method.");
        }

        // Used to extract the name of this player class.
        final String name() {
            String result = getClass().getName();
            return result.substring(result.indexOf('$')+1);
        }
    }

    /* Here are four simple strategies: */
    /* Given Agents */

    class NicePlayer extends Player {
        //NicePlayer always cooperates
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 0;
        }
    }

    class NastyPlayer extends Player {
        //NastyPlayer always defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 1;
        }
    }

    class RandomPlayer extends Player {
        //RandomPlayer randomly picks his action each time
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (Math.random() < 0.5)
                return 0;  //cooperates half the time
            else
                return 1;  //defects half the time
        }
    }

    class TolerantPlayer extends Player {
        //TolerantPlayer looks at his opponents' histories, and only defects
        //if at least half of the other players' actions have been defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int opponentCoop = 0;
            int opponentDefect = 0;
            for (int i=0; i<n; i++) {
                if (oppHistory1[i] == 0)
                    opponentCoop = opponentCoop + 1;
                else
                    opponentDefect = opponentDefect + 1;
            }
            for (int i=0; i<n; i++) {
                if (oppHistory2[i] == 0)
                    opponentCoop = opponentCoop + 1;
                else
                    opponentDefect = opponentDefect + 1;
            }
            if (opponentDefect > opponentCoop)
                return 1;
            else
                return 0;
        }
    }

    class FreakyPlayer extends Player {
        //FreakyPlayer determines, at the start of the match,
        //either to always be nice or always be nasty.
        //Note that this class has a non-trivial constructor.
        int action;
        FreakyPlayer() {
            if (Math.random() < 0.5)
                action = 0;  //cooperates half the time
            else
                action = 1;  //defects half the time
        }

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return action;
        }
    }

    class T4TPlayer extends Player {
		//Picks a random opponent at each play, 
		//and uses the 'tit-for-tat' strategy against them 
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0; //cooperate by default
            if (Math.random() < 0.5)
                return oppHistory1[n-1];
            else
                return oppHistory2[n-1];
        }
    }

	/* Additional Agents */

    class SoftT4TPlayer extends Player {
        // defect if either opponents defected in previous round
        // else cooperate
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0; //cooperate by default
            if ((oppHistory1[n-1]==0) || (oppHistory2[n-1]==0))
                return 0;
            else
                return 1;
        }
    }

	class HardT4TPlayer extends Player {
        // defect if both opponents defected in previous round
        // else cooperate
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0; //cooperate by default
            if ((oppHistory1[n-1]==0) && (oppHistory2[n-1]==0))
                return 0;
            else
                return 1;
        }
    }

	class GTPlayer extends Player {
		// defect for subsequent rounds if both opponents defected in the previous round
		// 'grim trigger' strategy
		boolean triggered = false;
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0;
			if (oppHistory1[n-1] + oppHistory2[n-1] == 2) triggered = true;
			if (triggered) return 1;
			return 0;
		}
	}

	class FT4TPlayer extends Player {
		// picks a random opponent at each round
		// if opponent defected >=10 times, player retaliates with defect
		// else player mimics the opponent's last move
		// 'forgiving tit-for-tat' strategy
		int forgivenessThreshold = 10;
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0; //cooperate by default
			else {
				int opponentDefectCount = 0;
				for (int i=0; i<n; i++) {
					if (Math.random() < 0.5) {
						if (oppHistory1[n-1] == 0) {
							opponentDefectCount++;
						}
					} else {
						if (oppHistory2[n-1] == 0) {
							opponentDefectCount++;
						}
					}
				}
				if (opponentDefectCount >= forgivenessThreshold) {
					return 0; // Retaliate after forgiveness threshold
				} else {
					if (Math.random() < 0.5) return oppHistory1[n-1];
            		else return oppHistory2[n-1];
				}
			}
        }
	}

	class GT4TPlayer extends Player {
        // picks a random opponent at each round,
		// if opponent cooperated in previous round, increase cooperation probability by 0.1
		// else decrease cooperation probability by 0.1
		// player's next move determined by generating random number
		// if random number < cooperation probability, cooperate 
		// else defect
        // 'generous tit-for-tat' strategy
		double cooperationProb = 0.9;
		boolean cooperate = true;
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0; //cooperate by default
            if (Math.random() < 0.5) {
				if (oppHistory1[n-1] == 0) {
					cooperationProb += 0.1;
					if (cooperationProb > 1) { // make sure probability is not more than 1
						cooperationProb = 1;
					}
				} else {
					cooperationProb -= 0.1;
					if (cooperationProb < 0.5) { // make sure probability is not negative
						cooperationProb = 0.5;
					}
				}
				if (Math.random() < cooperationProb) {
					return 1;
				}
				return 0;
			} else {
				if (oppHistory2[n-1] == 0) {
					cooperationProb += 0.1;
					if (cooperationProb > 1) { // make sure probability is not more than 1
						cooperationProb = 1;
					}
				} else {
					cooperationProb -= 0.1;
					if (cooperationProb < 0.5) { // make sure probability is not negative
						cooperationProb = 0.5;
					}
				}
				if (Math.random() < cooperationProb) {
					return 1;
				}
				return 0;
			}
        }
    }

	class AT4TPlayer extends Player {
        // picks a random opponent at each round,
		// defect if opponent cooperated in previous round
		// else cooperate
        // 'anti-tit-for-tat' strategy
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0; //cooperate by default
            if (Math.random() < 0.5) {
				if (oppHistory1[n-1] == 0) {
					return 1;
				}
				return 0;
			} else {
				if (oppHistory2[n-1] == 0) {
					return 1;
				}
				return 0;
			}
        }
    }

	class GPlayer extends Player {
        // picks a random opponent at each round
		// if opponent defected + forgiveness granted, cooperate with a certain probability
		// if opponent defected + forgiveness not granted, defect with a certain probability
		// else mimic opponent's previous move
        // 'gradual' strategy 
		double cooperationProbability = 0.5;
     	double forgivenessProbability = 0.2;
     	double defectionProbability = 0.8;
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0; //cooperate by default
            if (Math.random() < 0.5) {
				// Determine the next move based on the previous opponent move and forgiveness probability
				if (oppHistory1[n-1] == 0 && Math.random() < forgivenessProbability) {
					// Cooperate with a certain probability if the opponent defected and we forgive
					return (Math.random() < cooperationProbability) ? 1 : 0;
				} else if (oppHistory1[n-1] == 0) {
					// Defect with a certain probability if the opponent defected and we don't forgive
					return (Math.random() < defectionProbability) ? 0 : 1;
				} else {
					// Mimic the opponent's previous move
					return oppHistory1[n-1];
				}
			} else {
				// Determine the next move based on the previous opponent move and forgiveness probability
				if (oppHistory2[n-1] == 0 && Math.random() < forgivenessProbability) {
					// Cooperate with a certain probability if the opponent defected and we forgive
					return (Math.random() < cooperationProbability) ? 1 : 0;
				} else if (oppHistory2[n-1] == 0) {
					// Defect with a certain probability if the opponent defected and we don't forgive
					return (Math.random() < defectionProbability) ? 0 : 1;
				} else {
					// Mimic the opponent's previous move
					return oppHistory2[n-1];
				}
			}
		}
	}

    class PPlayer extends Player {
		// if payoff >=6 in previous round, do same move
		// else do opposite move
		// uses the 'pavlov' strategy 
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0;
            int r = n - 1;
            int myLA = myHistory[r]; int oppLA1 = oppHistory1[r]; int oppLA2 = oppHistory2[r];

            if (payoff[myLA][oppLA1][oppLA2]>=6) return myLA;
            return oppAction(myLA);
        }

        private int oppAction(int action) {
            if (action==1) return 0;
            return 1;
        }
    }

    /* My Agent */

	class SimpleMajorityPlayer extends Player {
		// Predicts opponents' moves based on majority of their past moves
		// And decides own moves accordingly
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponentCoop1 = 0, opponentCoop2 = 0;
			int predAction1, predAction2;

			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0) {
					opponentCoop1 += 1;
				}
				if (oppHistory2[i] == 0) {
					opponentCoop2 += 1;
				}
			}

			if (opponentCoop1 > n / 2)
				predAction1 = 0;
			else
				predAction1 = 1;

			if (opponentCoop2 > n / 2)
				predAction2 = 0;
			else
				predAction2 = 1;

			if (payoff[0][predAction1][predAction2] > payoff[1][predAction1][predAction2])
				return 0;

			return 1;
		}
	}

	class ExpectedUtilityPlayer extends Player {
		// Finds expected utility for each action
		// And performs action that maximises expected utility
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			float[][] probDists = new float[2][2];

			probDists[0] = findProbabilityDist(oppHistory1);
			probDists[1] = findProbabilityDist(oppHistory2);

			float coopUtil = findExpectedUtility(0, probDists);
			float defectUtil = findExpectedUtility(1, probDists);

			if (coopUtil > defectUtil)
				return 0;

			return 1;
		}

		float[] findProbabilityDist(int[] history) {
			float[] probDist = new float[2];

			for (int i = 0; i < history.length; i++) {
				probDist[history[i]]++;
			}

			probDist[0] = probDist[0] / history.length;
			probDist[1] = probDist[1] / history.length;

			return probDist;
		}

		float findExpectedUtility(int action, float[][] probDists) {
			float expectedUtility = 0;

			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					expectedUtility += probDists[0][j] * probDists[1][k] * payoff[action][j][k];
				}
			}

			return expectedUtility;
		}
	}

	class HybridPlayer extends ExpectedUtilityPlayer {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			float[] scores = calculateScores(myHistory, oppHistory1, oppHistory2);

			if (scores[1] < scores[0] || scores[2] < scores[0]) {
				return switchToSimpleMajority(n, myHistory, oppHistory1, oppHistory2);
			} else {
				return super.selectAction(n, myHistory, oppHistory1, oppHistory2);
			}

		}

		int switchToSimpleMajority(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponentCoop1 = 0, opponentCoop2 = 0;
			int predAction1, predAction2;

			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0) {
					opponentCoop1 += 1;
				}
				if (oppHistory2[i] == 0) {
					opponentCoop2 += 1;
				}
			}

			if (opponentCoop1 > n / 2)
				predAction1 = 0;
			else
				predAction1 = 1;

			if (opponentCoop2 > n / 2)
				predAction2 = 0;
			else
				predAction2 = 1;

			if (payoff[0][predAction1][predAction2] > payoff[1][predAction1][predAction2])
				return 0;

			return 1;
		}

		float[] calculateScores(int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int rounds = myHistory.length;
			float ScoreA = 0, ScoreB = 0, ScoreC = 0;

			for (int i = 0; i < rounds; i++) {
				ScoreA = ScoreA + payoff[myHistory[i]][oppHistory1[i]][oppHistory2[i]];
				ScoreB = ScoreB + payoff[oppHistory1[i]][oppHistory2[i]][myHistory[i]];
				ScoreC = ScoreC + payoff[oppHistory2[i]][myHistory[i]][oppHistory1[i]];
			}

			float[] result = { ScoreA / rounds, ScoreB / rounds, ScoreC / rounds };
			return result;
		}

	}

	// player 4
	class Jolene_Tan_Player extends HybridPlayer {
		int opp1Defects = 0;
		int opp2Defects = 0;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0)
				return 0;

			else {
				opp1Defects += oppHistory1[n - 1];
				opp2Defects += oppHistory2[n - 1];

				if (opp1Defects <= n / 2 && opp2Defects <= n / 2)
					return 0;

				if (opp1Defects > n / 2 && opp2Defects > n / 2)
					return 1;

				else {
					return super.selectAction(n, myHistory, oppHistory1, oppHistory2);
				}
			}
		}
	}

    /* In our tournament, each pair of strategies will play one match against each other.
     This procedure simulates a single match and returns the scores. */
    double[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
        int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
        double ScoreA = 0, ScoreB = 0, ScoreC = 0;

        for (int i=0; i<rounds; i++) {
            int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
            int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
            int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
            ScoreA = ScoreA + payoff[PlayA][PlayB][PlayC];
            ScoreB = ScoreB + payoff[PlayB][PlayC][PlayA];
            ScoreC = ScoreC + payoff[PlayC][PlayA][PlayB];
            HistoryA = extendIntArray(HistoryA, PlayA);
            HistoryB = extendIntArray(HistoryB, PlayB);
            HistoryC = extendIntArray(HistoryC, PlayC);
        }
        double[] result = {ScoreA/rounds, ScoreB/rounds, ScoreC/rounds};
        return result;
    }

    //	This is a helper function needed by scoresOfMatch.
    int[] extendIntArray(int[] arr, int next) {
        int[] result = new int[arr.length+1];
        for (int i=0; i<arr.length; i++) {
            result[i] = arr[i];
        }
        result[result.length-1] = next;
        return result;
    }

	/* The procedure makePlayer is used to reset each of the Players
	 (strategies) in between matches. When you add your own strategy,
	 you will need to add a new entry to makePlayer, and change numPlayers.*/

    Player makePlayer(int which) {
        switch (which) {
			// my agent
            case 0: return new Jolene_Tan_Player();
			// given agents
            case 1: return new NicePlayer();
            case 2: return new NastyPlayer();
            case 3: return new RandomPlayer();
            case 4: return new TolerantPlayer();
            case 5: return new FreakyPlayer();
            case 6: return new T4TPlayer();
            // additional agents
            case 7: return new SoftT4TPlayer();
			case 8: return new HardT4TPlayer();
            case 9: return new PPlayer();
            case 10: return new GTPlayer();
			case 11: return new FT4TPlayer();
			case 12: return new GT4TPlayer();
			case 13: return new AT4TPlayer();
			case 14: return new GPlayer();
        }
        throw new RuntimeException("Bad argument passed to makePlayer");
    }

    /* Modified main to run tournament rounds */
    public static void main (String[] args) {
		int TOURNAMENT_ROUNDS = 100; // change
		int NUM_PLAYERS = 15; // change
        boolean PRINT_TOP_3 = false;
        boolean VERBOSE = false; // set verbose = false if you get too much text output
        int val;

        ThreePrisonersDilemma_all instance = new ThreePrisonersDilemma_all();
        LinkedHashMap<Integer, Integer> hashMap = new LinkedHashMap<>(); // To store player's cumulative rankings.
        for (int player = 0; player < NUM_PLAYERS; player++)
            hashMap.put(player, 0); // initialize player's cumulative ranking to 0.
        for (int i = 0; i < TOURNAMENT_ROUNDS; i++) {
            int[] top_players = instance.runTournament(NUM_PLAYERS, VERBOSE);
            if (PRINT_TOP_3) for (int tp = 0; tp < 3; tp++) {
                System.out.println(top_players[tp]);
            }
            for (int p = 0; p < top_players.length; p++) {
                int tp = top_players[p];
                val = hashMap.get(tp);
                hashMap.put(tp, val + p + 1);
            }
        }
        // Sort players by cumulative ranking
        hashMap = (LinkedHashMap<Integer, Integer>) sortByValue(hashMap);
        float float_tournament_rounds = (float) TOURNAMENT_ROUNDS;
        float float_val;
        LinkedHashMap<Integer, Float> newHashMap = new LinkedHashMap<>();
        // Get average ranking
        for (int p=0; p<NUM_PLAYERS; p++) {
            val = hashMap.get(p);
            float_val = (float) val;
            newHashMap.put(p, float_val/float_tournament_rounds);
        }
        hashMap = (LinkedHashMap<Integer, Integer>) sortByValue(hashMap);
        newHashMap = (LinkedHashMap<Integer, Float>) sortByValue(newHashMap);

        System.out.print("[" + TOURNAMENT_ROUNDS + " TOURNAMENT_ROUNDS]");
        System.out.println(" >>> Player 0 is JOLENE_TAN_Player <<<");
        System.out.println("Summed up rankings for Players 0 to " + NUM_PLAYERS + " : " + hashMap);
        System.out.println("Average rankings : \t\t\t\t\t\t " + newHashMap);
    }

    int[] runTournament(int numPlayers, boolean verbose) {
        double[] totalScore = new double[numPlayers];
        // This loop plays each triple of players against each other.
        // Note that we include duplicates: two copies of your strategy will play once
        // against each other strategy, and three copies of your strategy will play once.
        int count = 0;
        for (int i=0; i<numPlayers; i++) for (int j=i; j<numPlayers; j++) for (int k=j; k<numPlayers; k++) {
            Player A = makePlayer(i); // Create a fresh copy of each player
            Player B = makePlayer(j);
            Player C = makePlayer(k);
            int rounds = 90 + (int)Math.rint(20 * Math.random()); // Between 90 and 110 rounds
            double[] matchResults = scoresOfMatch(A, B, C, rounds); // Run match
            totalScore[i] = totalScore[i] + matchResults[0];
            totalScore[j] = totalScore[j] + matchResults[1];
            totalScore[k] = totalScore[k] + matchResults[2];
            count++;
            if (verbose)
                System.out.println("[" + count + "] " + A.name() + " scored " + matchResults[0] +
                        " points, " + B.name() + " scored " + matchResults[1] +
                        " points, and " + C.name() + " scored " + matchResults[2] + " points.");
        }
        int[] sortedOrder = new int[numPlayers];
        // This loop sorts the players by their score.
        for (int i=0; i<numPlayers; i++) {
            int j=i-1;
            for (; j>=0; j--) {
                if (totalScore[i] > totalScore[sortedOrder[j]])
                    sortedOrder[j+1] = sortedOrder[j];
                else break;
            }
            sortedOrder[j+1] = i;
        }

        // Finally, print out the sorted results.
        if (verbose) System.out.println();
        System.out.println("Tournament Results");
        for (int i=0; i<numPlayers; i++)
            System.out.println("[Player " + sortedOrder[i] + "] " + makePlayer(sortedOrder[i]).name() + ": "
                    + totalScore[sortedOrder[i]] + " points.");

        System.out.println();
        return sortedOrder;
    } // end of runTournament()
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
//        Collections.reverse(list);

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
} // end of class PrisonersDilemma