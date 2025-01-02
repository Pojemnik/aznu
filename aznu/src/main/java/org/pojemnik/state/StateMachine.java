package org.pojemnik.state;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StateMachine
{
    public enum State { None, Created, Processing, Success, Error }

    @Getter
    private State state = State.None;

    public void create() throws IncorrectStateException
    {
        validateState(State.None);
        state = State.Created;
    }

    public void start() throws IncorrectStateException
    {
        validateState(State.Created);
        state = State.Processing;
    }

    public void error() throws IncorrectStateException
    {
        validateState(State.None, State.Created, State.Processing);
        state = State.Error;
    }

    public void finish() throws IncorrectStateException
    {
        validateState(State.Processing);
        state = State.Success;
    }

    private void validateState(State... expectedStates) throws IncorrectStateException
    {
        for (State expected : expectedStates)
        {
            if (state == expected)
            {
                return;
            }
        }
        throw new IncorrectStateException("Incorrect state %s. Expected one of %s".formatted(String.valueOf(state), Arrays.stream(expectedStates).map(String::valueOf).collect(Collectors.joining(","))));
    }
}
