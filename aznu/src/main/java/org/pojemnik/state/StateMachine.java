package org.pojemnik.state;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import org.pojemnik.state.IncorrectStateException;

public class StateMachine
{
    public enum State { Created, Processing, Success, Error }

    @Getter
    private State state = State.Created;

    public State start() throws IncorrectStateException
    {
        return changeState(State.Processing, List.of(State.Created));
    }

    public State error() throws IncorrectStateException
    {
        return changeState(State.Error, List.of(State.Created, State.Processing, State.Success));
    }

    public State finish() throws IncorrectStateException
    {
        return changeState(State.Success, List.of(State.Processing));
    }

    private synchronized State changeState(State target, List<State> expectedStates) throws IncorrectStateException
    {
        validateState(expectedStates);
        State last = state;
        state = target;
        return last;
    }

    private void validateState(List<State> expectedStates) throws IncorrectStateException
    {
        for (State expected : expectedStates)
        {
            if (state == expected)
            {
                return;
            }
        }
        throw new IncorrectStateException("Incorrect state %s. Expected one of %s".formatted(String.valueOf(state),
                expectedStates.stream().map(String::valueOf).collect(Collectors.joining(","))));
    }
}
