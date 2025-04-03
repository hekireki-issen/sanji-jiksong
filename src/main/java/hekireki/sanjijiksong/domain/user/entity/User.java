package hekireki.sanjijiksong.domain.user.entity;

import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String nickname;

	private String address;

	private Role role;

	private Boolean active;
}