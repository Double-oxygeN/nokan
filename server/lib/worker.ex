defmodule Brain.Worker do
  use GenServer

  def start_link(args) do
    GenServer.start_link(__MODULE__, args, name: __MODULE__)
  end

  def initialize() do
    GenServer.cast(__MODULE__, :init)
  end

  def show() do
    GenServer.call(__MODULE__, :show)
  end

  def push(e) do
    GenServer.cast(__MODULE__, {:push, e})
  end

  def add(e) do
    GenServer.cast(__MODULE__, {:add, e})
  end

  def amend(start_range, end_range, args) do
    GenServer.cast(__MODULE__, {:amend, start_range, end_range, args})
  end

  def replace(before_args, after_args) do
    GenServer.cast(__MODULE__, {:replace, before_args, after_args})
  end

  def init(start_args) do
    {:ok, start_args}
  end

  def handle_call(:show, _from, state) do
    {:reply, state, state}
  end

  def handle_cast(:init, _state) do
    {:noreply, "++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+."}
  end

  def handle_cast({:push, e}, _state) do
    {:noreply, e}
  end

  def handle_cast({:add, e}, state) do
    {:noreply, state <> "#{e}"}
  end

  def handle_cast({:amend, 0, e, args}, state) do
    {:noreply, args <> String.slice(state, e..-1)}
  end

  def handle_cast({:amend, s, e, args}, state) do
    {:noreply, String.slice(state, 0..(s - 1)) <> args <> String.slice(state, (e + 1)..-1)}
  end

  def handle_cast({:replace, b, a}, state) do
    {:noreply, String.replace(state, b, a)}
  end

  def terminate(_reason, _state) do

  end
end
