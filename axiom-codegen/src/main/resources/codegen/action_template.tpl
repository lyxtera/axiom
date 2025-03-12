package {0}.actions;

import com.lyxtera.axiom.config.Arg;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.api.model.BusinessAction;

/**
 * {1}
 */
@RuleMetadata(
    name = "{2}",
    description = "{3}"
)
public class {4} implements BusinessAction<YourContextKey> {

    /**
     * Execute the business action.
     * 
     */
    public Value execute({6}) {
        // TODO: Implement the action logic
        throw new UnsupportedOperationException("Not implemented yet");
    }
} 