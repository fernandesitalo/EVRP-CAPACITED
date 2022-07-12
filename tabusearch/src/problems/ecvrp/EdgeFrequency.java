package problems.ecvrp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class EdgeFrequency {
    public int from;
    public int to;
    public int cnt;
}