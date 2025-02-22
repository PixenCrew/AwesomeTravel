package renewal.awesome_travel.member.mapper;
import org.springframework.stereotype.Component;
import renewal.awesome_travel.member.dto.request.MemberRequestDto;
import renewal.awesome_travel.member.dto.response.MemberResponseDto;
import renewal.awesome_travel.member.entity.Member;
import renewal.awesome_travel.passport.dto.response.PassportResponseDto;
import renewal.awesome_travel.passport.mapper.PassportMapper;
import renewal.awesome_travel.wait.dto.response.WaitResponseDto;
import renewal.awesome_travel.wait.mapper.WaitMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component 
public class MemberMapper {

    // DTO -> Entity 변환
    public static Member memberDtoToMember(MemberRequestDto memberRequestDto) {
        if (memberRequestDto == null) {
            return null;
        }

        Member member = new Member(memberRequestDto.getEmail(), memberRequestDto.getName(), memberRequestDto.getNumber());
        return member;
    }

    // Entity -> DTO 변환
    public static MemberResponseDto memberToMemberDto(Member member) {
        if (member == null) {
            return null;
        }

        List<WaitResponseDto> waitListDto = member.getWaitList().stream()
                .map(WaitMapper::toDto) // Wait → WaitResponseDto 변환
                .collect(Collectors.toList());

        MemberResponseDto memberResponseDto = new MemberResponseDto();
        memberResponseDto.setName(member.getName());
        memberResponseDto.setEmail(member.getEmail());
        memberResponseDto.setNumber(member.getNumber());
        memberResponseDto.setGrade(member.getGrade());
        memberResponseDto.setPoint(member.getPoint());
        memberResponseDto.setPassport(PassportMapper.toDto(member.getPassport()));
        memberResponseDto.setWaitList(waitListDto);
        return memberResponseDto;
    }
}
