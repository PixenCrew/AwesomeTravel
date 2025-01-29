package renewal.awesome_travel.Member;
import org.springframework.stereotype.Component;

@Component 
public class MemberMapper {

    // DTO -> Entity 변환
    public Member memberDtoToMember(MemberDto memberDto) {
        if (memberDto == null) {
            return null;
        }

        Member member = new Member();
        member.setName(memberDto.getName());
        member.setEmail(memberDto.getEmail());
        member.setNumber(memberDto.getNumber());
        return member;
    }

    // Entity -> DTO 변환
    public MemberDto memberToMemberDto(Member member) {
        if (member == null) {
            return null;
        }

        MemberDto memberDto = new MemberDto();
        memberDto.setName(member.getName());
        memberDto.setEmail(member.getEmail());
        memberDto.setNumber(member.getNumber());
        return memberDto;
    }
}
