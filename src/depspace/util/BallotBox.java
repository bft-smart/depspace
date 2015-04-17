package depspace.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class BallotBox<I, V, B> {

	private final Set<I> ids;
	private final Map<V, List<B>> votes;
	private int decisionThreshold;
	
	
	public BallotBox() {
		this.ids = new HashSet<I>();
		this.votes = new HashMap<V, List<B>>();
	}

	
	@Override
	public String toString() {
		return "{" + ids + ", " + votes + "}";
	}

	
	public void init(int decisionThreshold) {
		this.decisionThreshold = decisionThreshold;
		clear();
	}
	
	public void clear() {
		ids.clear();
		votes.clear();
	}
	
	public int getVoteCount() {
		return ids.size();
	}
	
	public boolean hasVoted(I id) {
		return ids.contains(id);
	}

	public boolean add(I id, V vote, B ballot) {
		// Check whether 'id' has already cast a vote
		if(ids.contains(id)) return false;
		
		// Mark that 'id' has cast a vote
		ids.add(id);
		
		// Count vote
		List<B> ballots = votes.get(vote);
		if(ballots == null) {
			ballots = new ArrayList<B>(decisionThreshold);
			votes.put(vote, ballots);
		}
		ballots.add(ballot);
		return true;
	}
	
	public V getDecision() {
		// Check whether there is even a chance that a decision has been reached
		if(ids.size() < decisionThreshold) return null;
		
		// Check whether a decision has already been reached 
		for(Entry<V, List<B>> entry: votes.entrySet()) {
			if(entry.getValue().size() >= decisionThreshold) return entry.getKey();
		}
		return null;
	}
	
	public List<B> getDecidingBallots() {
		// Check whether there is even a chance that a decision has been reached
		if(ids.size() < decisionThreshold) return null;

		// Check whether a decision has already been reached 
		for(Entry<V, List<B>> entry: votes.entrySet()) {
			if(entry.getValue().size() >= decisionThreshold) return entry.getValue();
		}
		return null;
	}

	public List<B> getBallots() {
		List<B> ballots = new LinkedList<B>();
		for(List<B> ballotList: votes.values()) ballots.addAll(ballotList);
		return ballots;
	}

	public List<B> getBallots(V vote) {
		return votes.get(vote);
	}

	public boolean votesDiffer() {
		return votes.keySet().size() > 1;
	}

}
