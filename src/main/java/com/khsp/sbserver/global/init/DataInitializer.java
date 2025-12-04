package com.khsp.sbserver.global.init;

import com.khsp.sbserver.board.entity.BoardColumn;
import com.khsp.sbserver.board.repository.BoardColumnRepository;
import com.khsp.sbserver.user.entity.User;
import com.khsp.sbserver.user.enums.Role;
import com.khsp.sbserver.user.repository.UserRepositoty;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final BoardColumnRepository columnRepository;
    private final UserRepositoty userRepository;

    @Override
    public void run(String... args) throws Exception {
        // 컬럼이 하나도 없다면 기본 컬럼 생성
        if (columnRepository.count() == 0) {
            columnRepository.save(new BoardColumn("To Do", 1));
            columnRepository.save(new BoardColumn("In Progress", 2));
            columnRepository.save(new BoardColumn("Done", 3));
            System.out.println(">>> [Init] 기본 컬럼(To Do, In Progress, Done) 생성 완료.");
        }

        // 테스트 유저 생성
        if (userRepository.count() == 0) {
            userRepository.save(new User("manager@test.com", "1234", "매니저", Role.MANAGER));
            userRepository.save(new User("member@test.com", "1234", "일반회원", Role.MEMBER));
            System.out.println(">>> [Init] 테스트 유저(manager, member) 생성 완료.");
        }
    }
}
