package problems.ecvrp;

import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveWithCost {
    public Double cost;
    public List<Integer> mov;
}