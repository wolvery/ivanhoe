require File.dirname(__FILE__) + '/../test_helper'
require 'players_controller'

# Re-raise errors caught by the controller.
class PlayersController; def rescue_action(e) raise e end; end

class PlayersControllerTest < Test::Unit::TestCase
  fixtures :players

  def setup
    @controller = PlayersController.new
    @request    = ActionController::TestRequest.new
    @response   = ActionController::TestResponse.new
  end

  def test_should_get_index
    get :index
    assert_response :success
    assert assigns(:players)
  end

  def test_should_get_new
    get :new
    assert_response :success
  end
  
  def test_should_create_player
    old_count = Player.count
    post :create, :player => { }
    assert_equal old_count+1, Player.count
    
    assert_redirected_to player_path(assigns(:player))
  end

  def test_should_show_player
    get :show, :id => 1
    assert_response :success
  end

  def test_should_get_edit
    get :edit, :id => 1
    assert_response :success
  end
  
  def test_should_update_player
    put :update, :id => 1, :player => { }
    assert_redirected_to player_path(assigns(:player))
  end
  
  def test_should_destroy_player
    old_count = Player.count
    delete :destroy, :id => 1
    assert_equal old_count-1, Player.count
    
    assert_redirected_to players_path
  end
end
