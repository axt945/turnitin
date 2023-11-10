package integrations.turnitin.com.membersearcher.service;

import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class MembershipService {
	@Autowired
	private MembershipBackendClient membershipBackendClient;

	/**
	 * Method to fetch all memberships with their associated user details included.
	 * This method calls out to the php-backend service and fetches all memberships
	 * and also all the users, it thens adds each user individually to its
	 * corresponding membership.
	 *
	 * @return A CompletableFuture containing a fully populated MembershipList object.
	 */
	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
		CompletableFuture<Map<String, User>> userMap = membershipBackendClient.fetchUsers()
				.thenApply(users -> users.getUsers().stream()
						.collect(Collectors.toMap(User::getId, user -> user)));

		CompletableFuture<MembershipList> membershipList = membershipBackendClient.fetchMemberships();

		membershipList.thenCombine(userMap, (list, map) -> {
			list.getMemberships().forEach( member -> {
				if(map.containsKey(member.getUserId())) {
					member.setUser(map.get(member.getUserId()));
				}
			});
			return null;
		}).join();

		return membershipList;
	}

}
