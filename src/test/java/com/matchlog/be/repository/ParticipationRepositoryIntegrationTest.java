package com.matchlog.be.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.matchlog.be.TestcontainersConfiguration;
import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.constant.user.Provider;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.user.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

/** uk_participation_team_player 복합 유니크 제약이 실제 DB(MySQL) 레벨에서 걸리는지 검증. */
@Tag("integration")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ParticipationRepositoryIntegrationTest {

    @Autowired private ParticipationRepository participationRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void 같은_팀에_같은_선수를_두_번_저장하면_DataIntegrityViolationException이_발생한다() {
        User user =
                userRepository.save(
                        User.create("dup@example.com", "pw", "임준혁", null, Provider.LOCAL));
        Player player = playerRepository.save(Player.create(user, null, null, null, null, null));
        Team team = teamRepository.save(Team.create("FC 한강불사조", null, null, null, null, "DUP123"));

        participationRepository.saveAndFlush(
                Participation.create(player, team, ParticipationRole.PLAYER));

        assertThatThrownBy(
                        () ->
                                participationRepository.saveAndFlush(
                                        Participation.create(
                                                player, team, ParticipationRole.PLAYER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
