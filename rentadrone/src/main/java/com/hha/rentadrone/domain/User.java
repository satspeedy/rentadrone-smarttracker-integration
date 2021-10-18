package com.hha.rentadrone.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private static final String USER_ID_SEQUENCE = "USER_ID_SEQ";

    @Getter
    @Setter
    @Id
    @SequenceGenerator(name = USER_ID_SEQUENCE,
            sequenceName = USER_ID_SEQUENCE,
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = USER_ID_SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @Getter
    @Setter
    @Column(name = "name", nullable = false)
    private String name;

}
