class GamesController < ApplicationController

  layout "main_frame"
  
  before_filter :login_required, :except => [ :index, :show ]

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
    
    players_and_roles = GamePlayerList.find(:all, :conditions => "fk_game_id = #{@game.id}")
    raw_players = []
    players_and_roles.each do |list_row|
      raw_players << Player.find(list_row.fk_player_id)
    end
    
    @players = []
    raw_players.map { |player|   
      if( !@players.member? player )
       @players << player
      end
    }
    
    @roles = []
    players_and_roles.each do |list_row|
      @roles << Role.find(list_row.fk_role_id)
    end

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
    @game.fk_creator_id = @session['user'].id
    
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
