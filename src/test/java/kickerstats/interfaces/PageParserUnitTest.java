package kickerstats.interfaces;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kickerstats.GameTestdata;
import kickerstats.MatchTestdata;
import kickerstats.types.Game;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

public class PageParserUnitTest {

	private PageParser parser;
	private static Document begegnungDoc;

	private static Document begegnungNoDateDoc;
	private static Document begegnungenDoc;
	private static Document begegnungenNoDateDoc;
	private static Document begegnungBildDoc;
	private static Document begegnungenLiveDoc;
	private static Document uebersichtDoc;
	private static Document relegationDoc;
	private static Document begegnungNoNamesDoc;
	private static Document begegnungenNumberFormatExceptionDoc;

	@BeforeClass
	public static void loadTestFiles() throws IOException {
		begegnungDoc = loadFile("begegnung.html");
		begegnungenDoc = loadFile("begegnungen.html");
		begegnungenLiveDoc = loadFile("begegnungen_live.html");
		begegnungBildDoc = loadFile("begegnung_bild.html");
		begegnungNoDateDoc = loadFile("begegnung_no_date.html");
		uebersichtDoc = loadFile("uebersicht.html");
		relegationDoc = loadFile("relegation.html");
		begegnungNoNamesDoc = loadFile("begegnung_no_names.html");
		begegnungenNoDateDoc = loadFile("begegnungen_no_date.html");
		begegnungenNumberFormatExceptionDoc = loadFile("begegnungen_NFE.html");
	}

	@Before
	public void initialize() {
		parser = new PageParser();
	}

	@Test
	public void anUnconfirmedMatchIsNotAValidMatch() {
		Elements linkSnippets = parser.filterMatchLinkSnippets(begegnungenDoc);

		boolean isValid = parser.isValidMatchLink(linkSnippets.get(13));

		assertThat(isValid, is(false));
	}

	@Test
	public void aRunningMatchIsNotAValidMatch() {
		Elements linkSnippets = parser
				.filterMatchLinkSnippets(begegnungenLiveDoc);

		boolean isValid = parser.isValidMatchLink(linkSnippets.get(32));

		assertThat(isValid, is(false));
	}

	@Test
	public void aConfirmedMatchIsAValidMatch() {
		Elements linkSnippets = parser
				.filterMatchLinkSnippets(begegnungenLiveDoc);

		boolean isValid = parser.isValidMatchLink(linkSnippets.get(0));

		assertThat(isValid, is(true));
	}

	@Test
	public void returnsAllSeasons() throws IOException {
		List<Integer> expectedSeasonsIDs = Lists.newArrayList(7, 4, 3, 2, 1);

		List<Integer> seasonsIDs = parser.findSeasonIDs(uebersichtDoc);

		assertThat(seasonsIDs, is(expectedSeasonsIDs));
		assertThat(seasonsIDs.get(0), is(expectedSeasonsIDs.get(0)));
	}

	@Test
	public void returnsAllMatchLinks() throws IOException {
		List<String> matchLinks = parser.findMatchLinks(begegnungenDoc);

		assertThat(matchLinks.size(), is(14));
		String expectedMatchLink = "http://www.kickern-hamburg.de/liga-tool/mannschaftswettbewerbe?task=begegnung_spielplan&veranstaltungid=64&id=3815";
		assertThat(matchLinks.get(0), is(expectedMatchLink));
	}

	@Test
	public void returnsAllConfirmedMatchLinks() throws IOException {
		List<String> matchLinks = parser.findMatchLinks(begegnungenLiveDoc);

		assertThat(matchLinks.size(), is(24));
	}

	@Test
	public void returnsAllLigaLinks() throws IOException {
		List<String> ligaLinksIDs = parser.findLigaLinks(uebersichtDoc);

		assertThat(ligaLinksIDs.size(), is(5));
		assertThat(
				ligaLinksIDs.get(0),
				is("http://www.kickern-hamburg.de/liga-tool/mannschaftswettbewerbe?task=veranstaltung&veranstaltungid=8"));
	}

	@Test
	public void filtersAllMatchLinkSnippets() throws IOException {
		int matchCount = parser.filterMatchLinkSnippets(begegnungenDoc).size();

		assertThat(matchCount, is(110));
	}

	@Test
	public void filtersAllGames() throws IOException {
		int gameCount = parser.filterGameSnippets(begegnungDoc).size();

		assertThat(gameCount, is(16));
	}

	@Test
	public void detectsGamelistAsValid() throws IOException {
		Elements gameSnippets = parser.filterGameSnippets(begegnungDoc);

		assertThat(parser.isValidGameList(gameSnippets), is(true));
	}

	@Test
	public void detectsGameIsValidWithImagesAsValid() throws IOException {
		Elements gameSnippets = parser.filterGameSnippets(begegnungBildDoc);

		assertThat(parser.isValidGameList(gameSnippets), is(true));
	}

	@Test
	public void removesDescriptionFromTeamname() {
		String teamname = "Team Hauff (A)";
		assertThat(parser.removeTeamDescriptions(teamname), is("Team Hauff"));
	}

	@Test
	public void returnsCompleteTeamnameIfItDoesNotContainDescriptions() {
		String teamname = "Team Hauff";
		assertThat(parser.removeTeamDescriptions(teamname), is("Team Hauff"));
	}

	@Test
	public void returnsHomeTeamname() throws IOException {
		assertThat(parser.parseHomeTeam(begegnungDoc),
				is("Tingeltangel FC St. Pauli"));
	}

	@Test
	public void returnsGuestTeamname() throws IOException {
		assertThat(parser.parseGuestTeam(begegnungDoc),
				is("Hamburg Privateers 08"));
	}

	@Test
	public void detectsThatMatchHasNoMatchdate() throws IOException {
		assertThat(parser.hasMatchDate(begegnungNoDateDoc), is(false));
	}

	@Test
	public void detectsThatMatchHasAMatchdate() throws IOException {
		assertThat(parser.hasMatchDate(begegnungDoc), is(true));
	}

	@Test
	public void parsesAStringToDate() throws IOException {
		String rawDate = "27.02.2013 20:00";
		Calendar expectedDate = Calendar.getInstance();
		expectedDate.setTimeInMillis(0);
		expectedDate.set(2013, 1, 27, 20, 0);

		Date resultDate = parser.parseDate(rawDate);

		assertThat(resultDate, is(expectedDate.getTime()));
	}

	@Test
	public void parsesMatchdateFromMatchSnippet() throws IOException {
		Calendar expectedDate = Calendar.getInstance();
		expectedDate.clear();
		expectedDate.set(2013, 1, 27, 20, 0);

		assertThat(parser.parseMatchDate(begegnungDoc, true), is(expectedDate.getTime()));
	}

	@Test
	public void returnsCleanCalendarIfMatchHasNoMatchDate() throws IOException {
		Calendar expectedDate = Calendar.getInstance();
		expectedDate.setTimeInMillis(0);

		assertThat(parser.parseMatchDate(begegnungNoDateDoc, false),
				is(expectedDate.getTime()));
	}

	@Test
	public void parsesMatchday() throws IOException {
		assertThat(parser.parseMatchDay(begegnungDoc, true), is(1));
	}

	@Test
	public void parsesMatchdayWithoutDate() throws IOException {
		assertThat(parser.parseMatchDay(begegnungNoDateDoc, false), is(5));
	}

	@Test
	public void parsesHomeScore() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungDoc);

		assertThat(parser.parseHomeScore(rawGames.first(), false), is(4));
	}

	@Test
	public void parsesGuestScore() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungDoc);

		assertThat(parser.parseGuestScore(rawGames.first(), false), is(7));
	}

	@Test
	public void parsesHomeScoreFromGameSnippetWithImages() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungBildDoc);

		assertThat(parser.parseHomeScore(rawGames.first(), true), is(4));
	}

	@Test
	public void parsesGuestScoreFromGameSnippetWithImages() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungBildDoc);

		assertThat(parser.parseGuestScore(rawGames.first(), true), is(7));
	}

	@Test
	public void singleMatchIsNotADoubleMatch() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungDoc);

		assertThat(parser.isDoubleMatch(rawGames.first()), is(false));
	}

	@Test
	public void detectsDoubleMatch() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungDoc);

		assertThat(parser.isDoubleMatch(rawGames.last()), is(true));
	}

	@Test
	public void detectsDoubleMatchWithImages() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungBildDoc);

		assertThat(parser.isDoubleMatch(rawGames.last()), is(true));
	}

	@Test
	public void parsesPositionOfFirstGame() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungDoc);

		assertThat(parser.parseGamePosition(rawGames.first()), is(1));
	}

	@Test
	public void parsesPositionOfLastGame() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungDoc);

		assertThat(parser.parseGamePosition(rawGames.last()), is(16));
	}

	@Test
	public void parsesPlayerNamesOfLastGame() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungDoc);
		Game game = new Game();

		parser.addPlayerNames(game, rawGames.last(), true);

		assertThat(game.getHomePlayer1(), is("Sommer, Sebastian"));
		assertThat(game.getHomePlayer2(), is("Hölzer, Heinz"));
		assertThat(game.getGuestPlayer1(), is("Nestvogel, Markus"));
		assertThat(game.getGuestPlayer2(), is("Matheuszik, Sven"));
	}

	@Test
	public void parsesPlayerNamesOfFirstGame() throws IOException {
		Elements rawGames = parser.filterGameSnippets(begegnungDoc);
		Game game = new Game();

		parser.addPlayerNames(game, rawGames.first(), false);

		assertThat(game.getHomePlayer1(), is("Technau, Jerome"));
		assertThat(game.getGuestPlayer1(), is("Hojas, René"));
	}

	@Test
	public void returnsAllGamesFromAMatch() {
		List<Game> games = parser.findGames(begegnungDoc);

		assertThat(games.size(), is(16));
	}

	@Test
	public void returnsAllGamesWithImagesFromAMatch() {
		List<Game> games = parser.findGames(begegnungBildDoc);

		assertThat(games.size(), is(16));
	}

	@Test
	public void returnsAFullFilledSingleGame() {
		Game game = new Game();
		game.setDoubleMatch(false);
		game.setGuestPlayer1("Matheuszik, Sven");
		game.setGuestScore(7);
		game.setHomePlayer1("Kränz, Ludwig");
		game.setHomeScore(5);
		game.setPosition(2);

		List<Game> games = parser.findGames(begegnungDoc);

		assertThat(games.get(1), is(game));
	}

	@Test
	public void returnsAFullFilledDoubleGame() {
		Game game = new Game();
		game.setDoubleMatch(true);
		game.setGuestPlayer1("Zierott, Ulli");
		game.setGuestPlayer2("Hojas, René");
		game.setGuestScore(5);
		game.setHomePlayer1("Fischer, Harro");
		game.setHomePlayer2("Kränz, Ludwig");
		game.setHomeScore(4);
		game.setPosition(3);

		List<Game> games = parser.findGames(begegnungDoc);

		assertThat(games.get(2), is(game));
	}

	@Test
	public void returnsAFullFilledSingleGameWithImages() {
		Game game = GameTestdata.createSecondSingleGame();

		List<Game> games = parser.findGames(begegnungBildDoc);

		assertThat(games.get(0), is(game));
	}

	@Test
	public void returnsAFullFilledDoubleGameWithImages() {
		Game game = GameTestdata.createDoubleGame();

		List<Game> games = parser.findGames(begegnungBildDoc);

		assertThat(games.get(15), is(game));
	}

	@Test
	public void doesNotReturnGamesFromRelagation() {
		List<Game> games = parser.findGames(relegationDoc);

		assertThat(games.size(), is(0));
	}

	@Test
	public void parsesAllGamesInCorrectOrder() {
		List<Game> games = parser.findGames(begegnungNoNamesDoc);
		assertThat(games.get(0).getPosition(), is(1));
		assertThat(games.get(1).getPosition(), is(2));
		assertThat(games.get(2).getPosition(), is(3));
		assertThat(games.get(3).getPosition(), is(4));
		assertThat(games.get(4).getPosition(), is(5));
		assertThat(games.get(5).getPosition(), is(6));
		assertThat(games.get(6).getPosition(), is(7));
		assertThat(games.get(7).getPosition(), is(8));
		assertThat(games.get(8).getPosition(), is(9));
		assertThat(games.get(9).getPosition(), is(10));
		assertThat(games.get(10).getPosition(), is(11));
		assertThat(games.get(11).getPosition(), is(12));
		assertThat(games.get(12).getPosition(), is(13));
		assertThat(games.get(13).getPosition(), is(14));
		assertThat(games.get(14).getPosition(), is(15));
		assertThat(games.get(15).getPosition(), is(16));
	}

	@Test
	public void parsesGamesWithoutPlayernames() {
		List<Game> games = parser.findGames(begegnungNoNamesDoc);

		Game gameWithoutPlayernames = games.get(13);
		assertThat(gameWithoutPlayernames.getGuestPlayer1(), is(""));
		assertThat(gameWithoutPlayernames.getHomePlayer1(), is(""));
		assertThat(gameWithoutPlayernames.getHomeScore(), is(7));
		assertThat(gameWithoutPlayernames.getGuestScore(), is(0));
		assertThat(gameWithoutPlayernames.getPosition(), is(14));
		assertThat(gameWithoutPlayernames.isDoubleMatch(), is(false));
	}

	@Test
	public void returnsAFilledMatch() {
		List<MatchWithLink> matches = parser.findMatches(begegnungenDoc);
		MatchWithLink match = matches.get(0);

		assertThat(match, is(MatchTestdata.createMatchLink()));
	}

	@Test
	public void returnsAllMatchesWithoutNumberFormatException() {
		List<MatchWithLink> matches = parser.findMatches(begegnungenNumberFormatExceptionDoc);
		MatchWithLink match = matches.get(0);

		assertThat(match.getHomeGoals(), is(0));
		assertThat(match.getGuestGoals(), is(0));
		assertThat(match.getHomeScore(), is(8));
		assertThat(match.getGuestScore(), is(8));
	}
	
	@Test
	public void returnsTeamNamesWithoutDescriptions() {
		List<MatchWithLink> matches = parser.findMatches(begegnungenLiveDoc);
		MatchWithLink match = matches.get(0);

		assertThat(match.getHomeTeam(), is("2-5-3 kiggern.de Stade"));
	}
	
	@Test
	public void returnsAFilledMatchWithoutDate() {
		List<MatchWithLink> matches = parser.findMatches(begegnungenNoDateDoc);
		MatchWithLink match = matches.get(25);

		assertThat(match, is(MatchTestdata.createMatchLinkWithoutDate()));
	}

	@Test
	public void returnsAllMatches() {
		List<MatchWithLink> matches = parser.findMatches(begegnungenDoc);
		assertThat(matches.size(), is(14));
	}

	protected static Document loadFile(String fileName) throws IOException {
		File testFile = new File(PageParserTest.RECOURCES_DIRECTORY
				+ fileName);
		return Jsoup.parse(testFile, "UTF-8", "");
	}


}
