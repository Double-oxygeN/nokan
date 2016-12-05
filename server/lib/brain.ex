defmodule Brain do
  use Application

  def main(args) do
    start(nil, args)
  end

  def start(_type, _args) do
    import Supervisor.Spec, warn: false

    children = [
      worker(Brain.Worker, ["++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+."]),
      worker(Task, [Brain.TCPServer, :accept, [4040]])
    ]

    opts = [strategy: :one_for_one, name: Brain.Supervisor]
    Supervisor.start_link(children, opts)
  end
end
