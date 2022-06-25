package rpc;

public class HelloService implements IHelloService
{
    @Override
    public void hello() {
        System.out.println("Hello~");
    }
}
