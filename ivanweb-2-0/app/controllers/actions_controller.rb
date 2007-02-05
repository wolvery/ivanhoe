class ActionsController < ApplicationController

  # GET moves.xml
  def index
    @actions = Action.find(:all)
  
    respond_to do |format|
      format.xml  { render :xml => @actions.to_xml }
    end
  end
  
end
