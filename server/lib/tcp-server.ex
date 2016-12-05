defmodule Brain.TCPServer do
  require Logger

  def accept(port) do
    {:ok, socket} = :gen_tcp.listen(port, [:binary, packet: :line, active: false, reuseaddr: true])
    Logger.info "Accepting connections on port #{port}"
    loop_acceptor(socket)
  end

  def loop_acceptor(socket) do
    pid = spawn(__MODULE__, :serve, [])
    {:ok, client} = :gen_tcp.accept(socket)
    send pid, client
    loop_acceptor(socket)
  end

  def serve do
    receive do
      socket ->
        socket
        |> read_line
        |> prescript_to_worker
        |> write_line(socket)

        send self, socket
        serve
    end
  end

  def read_line(socket) do
    {:ok, data} = :gen_tcp.recv(socket, 0)
    data
  end

  def prescript_to_worker(args) do
    args |> Base.encode16 |> Logger.debug
    case args do
      "I" <> _rest ->
        Brain.Worker.initialize
        "initialized"
      "S" <> _rest ->
        Brain.Worker.show
      "P" <> rest ->
        rest |> String.trim("\n") |> Brain.Worker.push
        "pushed"
      "A" <> rest ->
        rest |> String.trim("\n") |> Brain.Worker.add
        "added"
      "M" <> rest ->
        [scd, thd, rst] = String.split(rest, "|")
        Brain.Worker.amend(String.to_integer(scd), String.to_integer(thd), String.trim(rst, "\n"))
        "amended"
      "R" <> rest ->
        [bef, aft] = String.split(rest, "|")
        Brain.Worker.replace(bef, String.trim(aft, "\n"))
        "replaced"
       _  -> args
    end
  end

  def write_line(line, socket) do
    :gen_tcp.send(socket, line <> <<13, 10>>)
  end
end
