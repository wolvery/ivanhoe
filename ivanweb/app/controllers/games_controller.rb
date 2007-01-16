class GamesController < ApplicationController

  layout "main_frame"
  
  before_filter :login_required, :except => [ :index, :show ]
  before_filter :game_creator_filter, :only => [ :new, :create, :edit, :update ]

  # GET /games
  # GET /games.xml
  def index
    @games = Game.find(:all)

    respond_to do |format|
      format.html # index.rhtml
      format.xml  { render :xml => @games.to_xml }
    end
  end

  # GET /games/1
  def show
    @game = Game.find(params[:id])    
    @players = GamePlayerList.get_players( @game.id )
  
    respond_to do |format|
      format.html
      format.xml  { render :xml => @game.to_xml }
    end
  end
  
  def launch
    @game = Game.find(params[:id])

    respond_to do |format|
      format.jnlp  { render :action => "launch", :layout => false }
    end
  end  

  # GET /games/new
  def new
    @game = Game.new
  end

  # GET /games/1;edit
  def edit
    @game = Game.find(params[:id])
  end

  # POST /games
  # POST /games.xml
  def create
    @game = Game.new(params[:game])
    
    # use ivanhoe's keyspace table for unique id
    keyspace = Keyspace.find( :first, :conditions => "tablename = 'game'" )
    @game.id = keyspace.next_value
    keyspace.next_value = keyspace.next_value + 1
    keyspace.save
    
    # the current user is the creator of this game
    @game.fk_creator_id = session['user'].id
    
    respond_to do |format|
      if @game.save
        flash[:notice] = 'Game was successfully created.'
        format.html { redirect_to game_url(@game) }
        format.xml  { head :created, :location => game_url(@game) }
      else
        format.html { render :action => "new" }
        format.xml  { render :xml => @game.errors.to_xml }
      end
    end
  end

  # PUT /games/1
  # PUT /games/1.xml
  def update
    @game = Game.find(params[:id])

    respond_to do |format|
      if @game.update_attributes(params[:game])
        flash[:notice] = 'Game was successfully updated.'
        format.html { redirect_to game_url(@game) }
        format.xml  { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @game.errors.to_xml }
      end
    end
  end

  # DELETE /games/1
  # DELETE /games/1.xml
  def destroy
    @game = Game.find(params[:id])
    @game.destroy

    respond_to do |format|
      format.html { redirect_to games_url }
      format.xml  { head :ok }
    end
  end
end
