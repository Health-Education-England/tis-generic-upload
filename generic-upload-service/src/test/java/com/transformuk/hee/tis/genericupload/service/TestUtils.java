package com.transformuk.hee.tis.genericupload.service;


import static org.mockito.internal.util.collections.Sets.newSet;

import com.transformuk.hee.tis.security.model.AuthenticatedUser;
import com.transformuk.hee.tis.security.model.UserProfile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.mockito.internal.util.collections.Sets;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class TestUtils {

  private static final String DBC = "1-85KJU0";

  public static void mockUserprofile(String userName, String... designatedBodyCodes) {
    UserProfile userProfile = new UserProfile();
    userProfile.setUserName(userName);
    userProfile.setDesignatedBodyCodes(Sets.newSet(designatedBodyCodes));
    AuthenticatedUser authenticatedUser = new AuthenticatedUser(userName, "dummyToekn", userProfile,
        null);
    UsernamePasswordAuthenticationToken authenticationToken = new
        UsernamePasswordAuthenticationToken(authenticatedUser, null);

    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
  }

  public static UserProfile mockWithPermissions(String userName, String... permissions) {
    UserProfile userProfile = new UserProfile();
    userProfile.setUserName(userName);
    userProfile.setDesignatedBodyCodes(newSet(DBC));
    Set<String> permSet = new HashSet<>(Arrays.asList(permissions));
    userProfile.setPermissions(permSet);
    return userProfile;
  }

}
