public class Tan_Jolene_Player extends Player {
	
    static int[][][] payoff = {  
		{{6,3},  //payoffs when first and second players cooperate 
		 {3,0}}, //payoffs when first player coops, second defects
		{{8,5},  //payoffs when first player defects, second coops
	     {5,2}}};//payoffs when first and second players defect
	
	private int oppDef1 = 0;
	private int oppDef2 = 0;
	
	int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
		// cooperate if first round
		if (n == 0) return 0;

		else {
			// calculate how many times each opponent previously defected
			oppDef1 += oppHistory1[n - 1];
			oppDef2 += oppHistory2[n - 1];

			// cooperate if both opponents mostly cooperated
			if (oppDef1 <= n / 2 && oppDef2 <= n / 2) return 0;

			// defect if both opponents mostly defected
			if (oppDef1 > n / 2 && oppDef2 > n / 2) return 1;

			// opponents have different majority moves
			else {
				// agent's current score
				float[] scores = calcScore(myHistory, oppHistory1, oppHistory2);

				// agent not worse performing - majority strategy
				if (scores[1] < scores[0] || scores[2] < scores[0]) {
					return switchToMajority(n, myHistory, oppHistory1, oppHistory2);
				}
				
				// agent worse performing - expected utility strategy
				else {
					float[][] probDist = new float[2][2];

					// find probability of each action for each opponent
					probDist[0] = calcProbDist(oppHistory1);
					probDist[1] = calcProbDist(oppHistory2);

					// find expected utility for cooperating and defecting
					float coopUtil = calcExpUtil(0, probDist);
					float defUtil = calcExpUtil(1, probDist);

					// choose action having higher expected utility
					if (coopUtil > defUtil) return 0;
                    
                    return 1;
				}
			}
		}
	}
	
	// majority strategy
	int switchToMajority(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
		int oppCoop1 = 0, oppCoop2 = 0;
		int predAction1, predAction2;

		// calculate how many times each opponent previously cooperated
		for (int i = 0; i < n; i++) {
			if (oppHistory1[i] == 0) oppCoop1 += 1;
			if (oppHistory2[i] == 0) oppCoop2 += 1;
		}

		// predict action of opponent 1 
		if (oppCoop1 > n / 2) predAction1 = 0;
		else predAction1 = 1;

		// predict action of opponent 2 
		if (oppCoop2 > n / 2) predAction2 = 0;
		else predAction2 = 1;

		// choose action that maximizes expected utility (payoff)
		if (payoff[0][predAction1][predAction2] > payoff[1][predAction1][predAction2])
			return 0;

		return 1;
	}

	// calculate scores of all players
	float[] calcScore(int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
		int numRounds = myHistory.length;
		float score1 = 0, score2 = 0, score3 = 0;

		for (int i = 0; i < numRounds; i++) {
			score1 = score1 + payoff[myHistory[i]][oppHistory1[i]][oppHistory2[i]];
			score2 = score2 + payoff[oppHistory1[i]][oppHistory2[i]][myHistory[i]];
			score3 = score3 + payoff[oppHistory2[i]][myHistory[i]][oppHistory1[i]];
		}

		float[] result = { score1 / numRounds, score2 / numRounds, score3 / numRounds };
		return result;
	}
	
	// calculate probability distribution of actions for an opponent
	float[] calcProbDist(int[] hist) {
		float[] probDist = new float[2];

		// find number of times opponent cooperated / defected
		for (int i = 0; i < hist.length; i++) {
			probDist[hist[i]]++;
		}

		// find probability that opponent cooperates / defects
		probDist[0] = probDist[0] / hist.length;
		probDist[1] = probDist[1] / hist.length;

		return probDist;
	}

	// calculate expected utility of agent performing an action 
	float calcExpUtil(int action, float[][] probDist) {
		float expUtil = 0;

		for (int j = 0; j < 2; j++) {
			for (int k = 0; k < 2; k++) {
				expUtil += probDist[0][j] * probDist[1][k] * payoff[action][j][k];
			}
		}

		return expUtil;
	}
}