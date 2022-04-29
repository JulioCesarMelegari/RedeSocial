package org.jcm.dto;

import lombok.Data;
import org.jcm.model.Follower;

@Data
public class FollowerResponse {
    private Long id;
    private String name;

    public FollowerResponse() {
    }

    public FollowerResponse(Follower follower) {
        id = follower.getId();
        name = follower.getFollower().getName();
    }

    public FollowerResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
