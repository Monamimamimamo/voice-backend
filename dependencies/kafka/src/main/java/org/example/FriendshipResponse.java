package org.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FriendshipResponse {
    private boolean Result;
    private String Description;

    @JsonCreator
    public FriendshipResponse(@JsonProperty("Result") boolean Result, @JsonProperty("Description") String Description) {
        this.Result = Result;
        this.Description = Description;
    }
}
