package hekireki.sanjijiksong.domain.user.entity;

import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import hekireki.sanjijiksong.global.common.exception.UserException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "`USERS`")
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


	// 탈퇴
	public void deactivate() {
		if (!this.active) {
			throw new UserException.UserAlreadyDeactivatedException();
		}
		this.active = false;
	}

	// 탈퇴 후 30일 이내인 경우만 복구 가능
	public boolean canBeRestored() {
		return this.getModifiedAt() != null && this.getModifiedAt().isAfter(LocalDateTime.now().minusDays(30));
	}

	// 복구
	public void restore() {
		if (Boolean.TRUE.equals(this.active)) {
			throw new UserException.UserAlreadyRestoredException(); // 이미 복구된 사용자
		}

		if (!canBeRestored()) {
			throw new UserException.UserRestoreExpiredException(); // 30일 초과
		}

		this.active = true;
	}

	// 비밀번호 변경
	public void updatePassword(String newPassword) {
		this.password = newPassword;
	}

}