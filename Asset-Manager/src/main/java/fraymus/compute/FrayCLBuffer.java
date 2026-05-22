package fraymus.compute;

import java.nio.ByteBuffer;

/**
 * 🧬 FRAYCL BUFFER
 * 
 * Memory buffer abstraction using DirectByteBuffer for device-like memory management
 * Provides typed read/write operations for common data types
 */
public class FrayCLBuffer {
    
    private final long id;
    private final ByteBuffer buffer;
    private final FrayCLContext context;
    private final long size;
    
    /**
     * Create a FrayCL buffer
     * 
     * @param id Buffer ID
     * @param buffer DirectByteBuffer for device-like memory
     * @param context Parent context
     */
    public FrayCLBuffer(long id, ByteBuffer buffer, FrayCLContext context) {
        this.id = id;
        this.buffer = buffer;
        this.context = context;
        this.size = buffer.capacity();
    }
    
    /**
     * Get buffer ID
     * 
     * @return Buffer ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Get buffer size in bytes
     * 
     * @return Buffer size
     */
    public long getSize() {
        return size;
    }
    
    /**
     * Get the underlying ByteBuffer
     * 
     * @return ByteBuffer instance
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    /**
     * Write float array to buffer
     * 
     * @param data Float array to write
     */
    public void writeFloatArray(float[] data) {
        buffer.clear();
        buffer.asFloatBuffer().put(data);
        buffer.flip();
    }
    
    /**
     * Read float array from buffer
     * 
     * @param data Array to fill with data
     */
    public void readFloatArray(float[] data) {
        buffer.clear();
        buffer.asFloatBuffer().get(data);
        buffer.flip();
    }
    
    /**
     * Read float array from buffer
     * 
     * @return Float array
     */
    public float[] readFloatArray() {
        int elements = (int) (size / 4);
        float[] data = new float[elements];
        readFloatArray(data);
        return data;
    }
    
    /**
     * Write double array to buffer
     * 
     * @param data Double array to write
     */
    public void writeDoubleArray(double[] data) {
        buffer.clear();
        buffer.asDoubleBuffer().put(data);
        buffer.flip();
    }
    
    /**
     * Read double array from buffer
     * 
     * @param data Array to fill with data
     */
    public void readDoubleArray(double[] data) {
        buffer.clear();
        buffer.asDoubleBuffer().get(data);
        buffer.flip();
    }
    
    /**
     * Write int array to buffer
     * 
     * @param data Int array to write
     */
    public void writeIntArray(int[] data) {
        buffer.clear();
        buffer.asIntBuffer().put(data);
        buffer.flip();
    }
    
    /**
     * Read int array from buffer
     * 
     * @param data Array to fill with data
     */
    public void readIntArray(int[] data) {
        buffer.clear();
        buffer.asIntBuffer().get(data);
        buffer.flip();
    }
    
    /**
     * Write byte array to buffer
     * 
     * @param data Byte array to write
     */
    public void writeByteArray(byte[] data) {
        buffer.clear();
        buffer.put(data);
        buffer.flip();
    }
    
    /**
     * Read byte array from buffer
     * 
     * @param data Array to fill with data
     */
    public void readByteArray(byte[] data) {
        buffer.clear();
        buffer.get(data);
        buffer.flip();
    }
    
    /**
     * Clear buffer (reset position to 0)
     */
    public void clear() {
        buffer.clear();
    }
    
    /**
     * Flip buffer (prepare for reading)
     */
    public void flip() {
        buffer.flip();
    }
    
    /**
     * Release buffer resources
     */
    public void release() {
        // DirectByteBuffer is managed by GC, but we can clear it
        buffer.clear();
        System.out.println(">>> [FRAYCL] Buffer released: ID=" + id);
    }
    
    @Override
    public String toString() {
        return "FrayCLBuffer{" +
                "id=" + id +
                ", size=" + size +
                " bytes}";
    }
}
