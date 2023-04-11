//package com.cz4046;

import java.util.*;

public class ThreePrisonersDilemma {

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

	/* additional agents (to keep) */

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

	/* additional agents (to discard) */
	// maybe keep
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

	// maybe keep
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

	/* https://github.com/almightyGOSU/CZ4046-Intelligent-Agents-Assignment-2/blob/master/src/ThreePrisonersDilemma.java */
	// player 1
    class Bummer extends NastyPlayer {

        //Count the number of defects by opp
        int intPlayer1Defects = 0;
        int intPlayer2Defects = 0;

        //Store the round where agent retaliate against defects
        int intRoundRetailate = -1;

        //Number of rounds where agent coop to observer opp actions
        int intObservationRound = 1;

        //Number of rounds where agent retaliate defects with defects
        //After this round, see opp actions to check if they decide to coop again
        int intGrudgeRound = 3;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            //Record Defects count
            if (n > 0) {
                intPlayer1Defects += oppHistory1[n - 1];
                intPlayer2Defects += oppHistory2[n - 1];
            }

            //Start by cooperating
            if (n < intObservationRound) {
                return 0; //cooperate by default
            }

            //Loop rounds where agent coop to reverse the effects of retaliation
            if (intRoundRetailate < -1) {
                intRoundRetailate += 1;
                intPlayer1Defects = 0;
                intPlayer2Defects = 0;

                return 0;
            }

            //Check at round retaliated + threshold to measure if opp wishes to coop again
            if (intRoundRetailate > -1 && n == intRoundRetailate + intGrudgeRound + 1) {

                //Count the number of coop during retaliate round to check opp coop level
                int intPlayer1Coop = 0;
                int intPlayer2Coop = 0;

                for (int intCount = 0; intCount < intGrudgeRound; intCount++) {
                    intPlayer1Coop += oppHistory1[n - 1 - intCount] == 0 ? 1 : 0;
                    intPlayer2Coop += oppHistory2[n - 1 - intCount] == 0 ? 1 : 0;
                    //intPlayer1Coop += oppHistory1[n - 1 - intCount] == 1 ? 1 : 0;
                    //intPlayer2Coop += oppHistory2[n - 1 - intCount] == 1 ? 1 : 0;
                }

                //If both players wish to coop again, start to coop with them
                if (intPlayer1Coop > 1 && intPlayer2Coop > 1 && (oppHistory1[n - 1] + oppHistory2[n - 1]) == 0) {
                    //Hold round where agent coop to show intention to coop again
                    //Count backwards from -2
                    //-2 indicates 1 round where agent coop to reverse effect of retailation
                    //-5 indicates 4 rounds where agent coop to reverse effect
                    intRoundRetailate = -2;

                    intPlayer1Defects = 0;
                    intPlayer2Defects = 0;

                    return 0;
                } else {
                    intRoundRetailate = n;
                    return 1;
                }

            }

            //Punish Defection by defecting straight away
            //Stores the round defected
            if (intPlayer1Defects + intPlayer2Defects > 0) {
                intRoundRetailate = n;
                return 1;
            }

            //Coop as default action
            return 0;
        }
    }

	// player 2
    class GosuTheMinion extends NicePlayer {

        // For tracking Defect/Cooperate probabilities
        private double opp1Def = 0;
        private double opp2Def = 0;

        // Thresholds
        private static final double FRIENDLY_THRESHOLD = 0.850;
        private static final double DEFENSIVE_THRESHOLD = 0.750;

        /* ALL HAIL KING CHODY!! */
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            // Start by cooperating
            if (n == 0) {

                return 0;
            }

            // Calculate probability for Def/Coop (Opponent 1)
            opp1Def += oppHistory1[n - 1];
            double opp1DefProb = opp1Def / oppHistory1.length;
            double opp1CoopProb = 1.000 - opp1DefProb;

            // Calculate probability for Def/Coop (Opponent 2)
            opp2Def += oppHistory2[n - 1];
            double opp2DefProb = opp2Def / oppHistory2.length;
            double opp2CoopProb = 1.000 - opp2DefProb;

            /*System.out.printf("Opponent 1: %.3f, %.3f, Opponent 2: %.3f, %.3f%n",
					opp1CoopProb, opp1DefProb, opp2CoopProb, opp2DefProb);*/
            if (opp1CoopProb >= FRIENDLY_THRESHOLD
                    && opp2CoopProb >= FRIENDLY_THRESHOLD
                    && oppHistory1[n - 1] == 0
                    && oppHistory2[n - 1] == 0) {

                // Good chance that both opponents will cooperate
                // Just cooperate so that everyone will be happy
                return 0;

            } else if ((opp1DefProb >= DEFENSIVE_THRESHOLD || opp2DefProb >= DEFENSIVE_THRESHOLD)
                    && (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1)) {

                // Given that one of the opponents have been relatively nasty,
                // and one of them has defected in the previous turn,
                // high prob that one of them will defect again,
                // defect to protect myself!
                return 1;

            } else if (n >= 2) {

                // Check if either opponent has defected in the last 2 turns
                if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1
                        || oppHistory1[n - 2] == 1 || oppHistory2[n - 2] == 1) {

                    // DESTROY them!!
                    return 1;
                } else {

                    // Just be friendly!
                    return 0;
                }
            } else {

                // At this moment, both players are not that friendly,
                // and yet neither of them are relatively nasty.
                // Just be friendly for now.
                return 0;
            }
        }
    }

	// player 3
	class PM_Low extends Player {

        int myScore = 0;
        int opp1Score = 0;
        int opp2Score = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            if (n == 0) {
                return 0; // cooperate by default
            }

            // get the recent history index
            int i = n - 1;

            // add up the total score/points for each player
            myScore += payoff[myHistory[i]][oppHistory1[i]][oppHistory2[i]];
            opp1Score += payoff[oppHistory1[i]][oppHistory2[i]][myHistory[i]];
            opp2Score += payoff[oppHistory2[i]][myHistory[i]][oppHistory1[i]];

            // if my score is lower than the any of them
            // it means that at least one of them have defected
            if (myScore >= opp1Score && myScore >= opp2Score) {

                // cooperate if my score is higher or equal than all of them
                return 0;
            }

            return 1; // defect if my score is lower than any of them
        }
    }

	/* https://github.com/Javelin1991/CZ4046_Intelligent_Agents/blob/master/CZ4046_Assignment_2/ThreePrisonersDilemma.java */
	
	class Naing_Htet_Player extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            // Rule 1: our agent will cooperate in the first round
            if (n == 0)  {
                return 0;
            }

            // Rule 2: our agent will defect in the last few rounds, NastyPlayer mode is turned on
            if (n > 95) {
                return 1;
            }

            // Rule 3: if all players including our agent cooperated in the previous round,
            // then our agent will continue to cooperate
            if (myHistory[n-1] == 0 && oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0) {
                return 0;
            }

            // Rule 4: check opponents history to see if they have defected before
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 1 || oppHistory2[i] == 1) {
                    // if either one of them defected before, our agent will always defect
                    return 1;
                }
            }
            // Rule 5: Otherwise, by default nature, our agent will always cooperate
            return 0;
        }
    }

	/* https://github.com/wayneczw/cz4046/blob/master/assignment2/Main/ThreePrisonersDilemma.java */
	class Chen_Zhiwei_Player extends Player {
		int myScore = 0;
		int opp1Score = 0;
		int opp2Score = 0;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// First Law: Always cooperate in first 2 rounds
			if (n < 2) return 0;

			// Second Law: Tolerate 2 consecutive defects from both opp
			// If 2 consecutive defects from both opp, then defect
			if (oppHistory1[n-1] == 1 && oppHistory1[n-2] == 1 &&
				oppHistory2[n-1] == 1 && oppHistory2[n-2] == 1)
				return 1;

			// Third Law: if one of the opponents is Nasty, then always defect
			boolean isOpp1Nasty, isOpp2Nasty;
			isOpp1Nasty = isNasty(n, oppHistory1);
			isOpp2Nasty = isNasty(n, oppHistory2);
			if (isOpp1Nasty || isOpp2Nasty) return 1;

			// Fourth Law: if one of the opponents is Random, then always defect
			boolean isOpp1Random, isOpp2Random;
			isOpp1Random = isRandom(n, oppHistory1);
			isOpp2Random = isRandom(n, oppHistory2);
			if (isOpp1Random || isOpp2Random) return 1;

			// Fifth Law: if my current score is lower than one of the opp, then always defect
			myScore += payoff[myHistory[n-1]][oppHistory1[n-1]][oppHistory2[n-1]];
			opp1Score += payoff[oppHistory1[n-1]][oppHistory2[n-1]][myHistory[n-1]];
			opp2Score += payoff[oppHistory2[n-1]][oppHistory1[n-1]][myHistory[n-1]];
			if (myScore < opp1Score || myScore < opp2Score) return 1;

			// Sixth Law: If above laws don't apply, then be a T4TPlayer
			if (Math.random() < 0.5) return oppHistory1[n-1];
			else return oppHistory2[n-1];
		}

		boolean isNasty(int n, int[] oppHistory) {
			int cnt = 0;
			for (int i=0; i<n; i++){
				if (oppHistory[i] == 1)
					cnt++;
			}

			if (cnt == n) return true;
			else return false;
		}

		boolean isRandom(int n, int[] oppHistory) {
			int sum = 0;
			double eps = 0.025;
			for (int i=0; i<n; i++) {
				sum += oppHistory[i];
			}

			// if ratio is roughly 0.5, then the opponent is highly likely to be random
			double ratio = (double) sum / n;
			if (Math.abs(ratio - 0.5) < eps) return true;
			else return false;
		}
	}

	/* https://github.com/ShreyasMundhra/CZ4046-Assignment2/blob/master/src/Main/ThreePrisonersDilemma.java */
	// player 1
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

	// player 2
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

	// player 3
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
	class DonaldDuck extends HybridPlayer {
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

	/* Acharya_Atul_Player */
	public class Acharya_Atul_Player extends Player {

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
	
			// Rule 1
			if (n == 0) return 0; //cooperate by default
	
			// Rule 2
			if (n >= 99) return 1; // defect in the end
	
			// Rule 3, 4, 5
	
			/* 
			If oppHistory[n-1] == oppHistory2[n-1] then either both cooperated or both defected the previous round,
			hence we can just return the action that one of them played in the previous round
	
			Else if oppHistory[n-1] != oppHistory[n-2] then only 1 of them cooperated the previous round.
			This corresponds to Rule 5: playTolerantAction 
			*/
			return oppHistory1[n-1] == oppHistory2[n-1] ? oppHistory1[n-1]:
			playTolerantAction(n, myHistory, oppHistory1, oppHistory2);
		}
	
		// Implentation of Rule 5: Selecting a Tolerant Action 
		int playTolerantAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponent1 = 0;
			int opponent2 = 0;
	
			for (int i = 0 ; i < n; ++i){
				opponent1 += (oppHistory1[i] == 0? 1 : -1);
				
				opponent2 += (oppHistory2[i] == 0? 1: -1);
			}
	
			// If the value > 0 then ooponent has cooperated more times than defected
			// Only if both values > 0 then we cooperate, else defect
	
			if (opponent1 >= 0 && opponent2 >= 0) return 0;
			return 1;
	
		}
	}

	/* Wu_Rongxi_Player */
	class RX4 extends Player {


		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			if (n==0)
				return 0;   //cooperate in initial round

			if(oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];   //follow whatever my opponents do in the previous round if they did the same as each other

			else
					return myHistory[n-1];   //else return my own last move


		}

	}

	/* Huang_KyleJunyuan_Player */
	class Huang_KyleJunyuan_Player extends Player {
		// Helper function to calculate percentage of cooperation
		float calCoopPercentage(int[] history) {
			int cooperates = 0;
			int length = history.length;
	
			for (int i = 0; i < length; i++)
				if (history[i] == 0)
					cooperates++;
	
			return (float) cooperates / length * 100;
		}
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0)
				return 0; // First round: Cooperate
	
			/* 1. Calculate percentage of cooperation */
			float perOpp1Coop = calCoopPercentage(oppHistory1);
			float perOpp2Coop = calCoopPercentage(oppHistory2);
	
			/* 2. If both players are mostly cooperating */
			if (perOpp1Coop > 90 && perOpp2Coop > 90) {
				int range = (10 - 5) + 1; // Max: 10, Min: 5
				int random = (int) (Math.random() * range) + 5;
				
				if (n > (90 + random))  // Selfish: Last min defect
					return 1;
				else
					return 0;	// First ~90 rounds: Cooperate
			}
	
			/* 3. Defect by default */
			return 1;
		}
	}

	/* Kong_JieWei_Player */
	class Kong_JieWei_Player extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// start by cooperating in the first round
			if (n==0) return 0;

			if (n==1) {
				// check if any opponent defected in the first round
				if (oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0) return 0;
				else return 1;
			}

			// after first 2 rounds
			// check if any opponent defected recently in the previous 2 rounds
			if (oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0
					&& oppHistory1[n-2] == 0 && oppHistory2[n-2] == 0) {
				// calculate opponents' defection rate
				double opp1DefectRate, opp2DefectRate;
				opp1DefectRate = opp2DefectRate = 0;
				for (int i = 0; i < n; i ++) {
					opp1DefectRate += oppHistory1[i];
					opp2DefectRate += oppHistory2[i];
				}
				opp1DefectRate /= n;
				opp2DefectRate /= n;

				double DEFECT_THRESH = 0.1; // max defection rate to tolerate
				if (opp1DefectRate > DEFECT_THRESH || opp2DefectRate > DEFECT_THRESH) {
					double FORGIVE_THRESH = 0.1; // probability of forgiveness

					// consider forgiving
					if (Math.random() < FORGIVE_THRESH) return 0;

					// don't forgive
					else return 1;
				}

				//cooperate if both opponents less than my defection threshold
				return 0;
			}
			// defect to save myself if any opponent has recently defected
			else {
				return 1;
			}
		}
	}


	/* my agent */

    class JOLENE_TAN_Player extends Player {
        //final String NAME = "[██] WILSON_THURMAN_TENG";
        //final String MATRIC_NO = "[██] U1820540H";

        int[][][] payoff = {
                {{6, 3},     //payoffs when first and second players cooperate
                        {3, 0}},     //payoffs when first player coops, second defects
                {{8, 5},     //payoffs when first player defects, second coops
                        {5, 2}}};    //payoffs when first and second players defect

        int r;
        int[] myHist, opp1Hist, opp2Hist;
        int myScore=0, opp1Score=0, opp2Score=0;
        int opponent1Coop = 0; int opponent2Coop = 0;

        final double LENIENT_THRESHOLD = 0.705; // Used for Law [#1]
        final double STRICT_THRESHOLD = 0.750; // Used for Law [#2]

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            /**
             LAWS:
             [#0] Unless I am losing, be trustworthy and unpredictable at the same time.
             [#1] Protect myself.
             [#2] Cooperate in a cooperative environment.
             [#3] If I am losing, turn it into a lose-lose situation.
             */

            // Assume environment is cooperative. Always cooperate in first round!
            if (n==0) return 0;

            // Updating class variables for use in methods.
            this.r = n - 1; // previous round index
            this.myHist = myHistory;
            this.opp1Hist = oppHistory1;
            this.opp2Hist = oppHistory2;

            // Updating Last Actions (LA) for all players.
            int myLA = myHistory[r];
            int opp1LA = oppHistory1[r];
            int opp2LA = oppHistory2[r];

            // Updating Scores for all players
            this.myScore += payoff[myLA][opp1LA][opp2LA];
            this.opp1Score += payoff[opp1LA][opp2LA][myLA];
            this.opp2Score += payoff[opp2LA][opp1LA][myLA];

            // Update opponent's cooperate record.
            if (n>0) {
                opponent1Coop += oppAction(opp1Hist[r]);
                opponent2Coop += oppAction(opp2Hist[r]);
            }
            // Calculate opponent's cooperate probability.
            double opponent1Coop_prob = opponent1Coop / opp1Hist.length;
            double opponent2Coop_prob = opponent2Coop / opp2Hist.length;

            /** [PROTECT MYSELF]: -> Law [#1]
             When it is nearing the end of the tournament at 100 rounds, if both players are known to be relatively nasty
             (cooperate less than 75% of the time). Defect to protect myself.
             */
            if ((n>90) && (opponent1Coop_prob<STRICT_THRESHOLD && opponent2Coop_prob<STRICT_THRESHOLD)) { // change
                // Law [#0] Added
                return actionWithNoise(1, 99);
            }

            /** [REWARD COOPERATION]: -> Law [#2]
             At any point in time before we are able to accurately decide if opponents are nasty or not. We set a lenient
             threshold (0.705) to gauge if opponents are cooperative. Additionally, we check if both opponent's last action
             was to cooperate. If yes, we will cooperate too.
             */
            if ((opp1LA+opp2LA ==0)&&(opponent1Coop_prob>LENIENT_THRESHOLD && opponent2Coop_prob>LENIENT_THRESHOLD)) {
                // Law [#0] Added
                return actionWithNoise(0, 99);
            }
            else
            /** [I WILL NOT LOSE] -> Law [#3]
             However, if opponent is not cooperative, we will check if we have the highest score.
             If we have the highest score, we are appeased and will cooperate. Else, we will defect.
             */
                return SoreLoser();
        }

        /**
         * Law [#0]: This utility method introduces noise to an agent's action, allowing it to be unpredictable.
         * @param intendedAction The agent's intended action.
         * @param percent_chance_for_intended_action The percentage chance the agent will perform it's intended action.
         * @return The agent's final action.
         */
        private int actionWithNoise(int intendedAction, int percent_chance_for_intended_action) {
            Map<Integer, Integer> map = new HashMap<Integer, Integer>() {{
                put(intendedAction, percent_chance_for_intended_action);
                put(oppAction(intendedAction), 1-percent_chance_for_intended_action);
            }};
            LinkedList<Integer> list = new LinkedList<>();
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    list.add(entry.getKey());
                }
            }
            Collections.shuffle(list);
            return list.pop();
        }

        /** Law [#3]:
         * Cooperates if agent currently has the highest score, else defect.
         * @return
         */
        private int SoreLoser() {
            if (iAmWinner()) return 0;
            return 1;
        }

        /* Function to check if agent is loser or not. Agent is a winner if it has the highest score. */
        private boolean iAmWinner() {
            if (myScore>=opp1Score && myScore>=opp2Score) {
                return true;
            }
            return false;
        }
        /* Utility method to obtain opposite action. */
        private int oppAction(int action) {
            if (action == 1) return 0;
            return 1;
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
            case 0: return new JOLENE_TAN_Player();
			// original agents
            case 1: return new NicePlayer();
            case 2: return new NastyPlayer();
            case 3: return new RandomPlayer();
            case 4: return new TolerantPlayer();
            case 5: return new FreakyPlayer();
            case 6: return new T4TPlayer();
            // additional agents to keep
            case 7: return new PPlayer();
            case 8: return new GTPlayer();
			case 9: return new FT4TPlayer();
			case 10: return new GT4TPlayer();
			case 11: return new AT4TPlayer();
			case 12: return new GPlayer();
			// additional agents to discard
			case 13: return new SoftT4TPlayer();
			case 14: return new HardT4TPlayer();
			case 15: return new Bummer();
			case 16: return new GosuTheMinion();
			case 17: return new PM_Low();
			case 18: return new Naing_Htet_Player();
			case 19: return new Chen_Zhiwei_Player();
			case 20: return new SimpleMajorityPlayer();
			case 21: return new ExpectedUtilityPlayer();
			case 22: return new HybridPlayer();
			case 23: return new DonaldDuck();
			case 24: return new Acharya_Atul_Player();
			case 25: return new RX4();
			case 26: return new Huang_KyleJunyuan_Player();
			case 27: return new Kong_JieWei_Player();

        }
        throw new RuntimeException("Bad argument passed to makePlayer");
    }

    /* Modified main to run tournament rounds */
    public static void main (String[] args) {
		int TOURNAMENT_ROUNDS = 100; // change
		int NUM_PLAYERS = 28; // change
        boolean PRINT_TOP_3 = false;
        boolean VERBOSE = false; // set verbose = false if you get too much text output
        int val;

        ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
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
