package ma.hemzastudio.bestlogspractices.web.dto.request;

import lombok.Data;

public class UserCommand{

    @Data
    public static class Create {
//        @NotBlank(message = "Email is required")
//        @Email(message = "Must be a valid email address")
        private String email;
//        @NotBlank(message = "Username is required")
//        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;
    }


    @Data
    public class Update {
//        @Email(message = "Must be a valid email address")
        private String email;
//        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;
    }

}
