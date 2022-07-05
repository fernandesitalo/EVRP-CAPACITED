package problems.ecvrp;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Movement {
    public Integer type;
    List<Integer> indexes;
}
