class PlayersController < ApplicationController

  before_filter :admin_filter, :except => :show

  layout "main_frame"
  

  # GET /players
  # GET /players.xml
  def index
    @players = Player.find(:all)

    respond_to do |format|
      format.html # index.rhtml
      format.xml  { render :xml => @players.to_xml }
    end
  end

  # GET /players/1
  # GET /players/1.xml
  def show
    @player = Player.find(params[:id])

    respond_to do |format|
      format.html # show.rhtml
      format.xml  { render :xml => @player.to_xml }
    end
  end

  # GET /players/1;edit
  def edit
    @player = Player.find(params[:id])
  end

  # PUT /players/1
  # PUT /players/1.xml
  def update
    @player = Player.find(params[:id])
    @player.password_confirmation = @player.password

    respond_to do |format|
      if @player.update_attributes(params[:player])
        flash[:notice] = 'Player was successfully updated.'
        format.html { redirect_to player_url(@player) }
        format.xml  { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @player.errors.to_xml }
      end
    end
  end

  # DELETE /players/1
  # DELETE /players/1.xml
  def destroy
    @player = Player.find(params[:id])
    @player.password_confirmation = @player.password
    @player.destroy

    respond_to do |format|
      format.html { redirect_to players_url }
      format.xml  { head :ok }
    end
  end
  
  protected
  
  def admin_filter
    (session['user'] and session['user'].admin)
  end
  
end
