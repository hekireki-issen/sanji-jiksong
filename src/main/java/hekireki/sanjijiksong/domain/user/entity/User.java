package hekireki.sanjijiksong.domain.user.entity;

import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import hekireki.sanjijiksong.global.common.exception.UserException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "users")
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

	@Enumerated(EnumType.STRING)
	private Role role;

	private Boolean active;


	//탈퇴
	public void deactivate() {
		if (!this.active) {
			throw new UserException.UserAlreadyDeactivatedException();
		}
		this.active = false;
	}

	//복구
	public boolean restore() {
		if (this.active) return false; // 이미 활성 상태
		this.active = true;
		return true;
	}
}