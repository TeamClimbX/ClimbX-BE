package com.climbx.climbx.user.service;

import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserLookupService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountEntity findUserByNickname(String nickname) {
        return userAccountRepository.findByNickname(nickname)
            .orElseThrow(() -> new UserNotFoundException(nickname));
    }

    public UserAccountEntity findUserById(Long userId) {
        return userAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * 모든 사용자 조회 (@SQLRestriction으로 USER role 자동 필터링)
     */
    public List<UserAccountEntity> findAllUsers() {
        return userAccountRepository.findAll();
    }

    /**
     * 닉네임에 특정 문자열을 포함하는 사용자들 조회 (@SQLRestriction으로 USER role 자동 필터링)
     */
    public List<UserAccountEntity> findUsersByNicknameContaining(String nickname) {
        return userAccountRepository.findByNicknameContaining(nickname);
    }
}