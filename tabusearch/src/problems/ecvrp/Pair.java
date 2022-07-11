package problems.ecvrp;

import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pair {
    public Double cost;
    public List<Integer> mov;
}