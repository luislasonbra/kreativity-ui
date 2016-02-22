package com.katzstudio.kreativity.ui;

/**
 * Generic model used by Kreativity components. It contains callbacks for edit begin / end so implementations
 * can implement undo / redo while skipping intermediate changes (such as dragging a spinner or entering characters
 * in a text field).
 */
public interface Model<T> {

    /**
     * Returns the current value of the model.
     *
     * @return the model's value
     */
    T getValue();

    /**
     * Sets the value of the model. This can be an ephemeral intermediate change.
     *
     * @param value the new value
     */
    void setValue(T value);

    /**
     * Called before the ui component starts making ephemeral changes to the model.
     */
    void ephemeralChangesBegin();

    /**
     * Called after the ui component stops making ephemeral changes to the model.
     */
    void ephemeralChangesEnd();

    class Empty<T> implements Model<T> {

        @Override
        public T getValue() {
            return null;
        }

        @Override
        public void setValue(T value) {
        }

        @Override
        public void ephemeralChangesBegin() {
        }

        @Override
        public void ephemeralChangesEnd() {
        }
    }
}
